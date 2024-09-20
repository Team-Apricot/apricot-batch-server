package sandbox.apricot.youth.service;

import static sandbox.apricot.util.Region.SEOUL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import sandbox.apricot.util.AgeInfoFormatter;
import sandbox.apricot.util.ExtractFormatter;
import sandbox.apricot.util.PeriodFormatter;
import sandbox.apricot.youth.dto.response.PolicyDto;
import sandbox.apricot.youth.entity.Policy;
import sandbox.apricot.youth.repository.PolicyRepository;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PolicyServiceImpl implements PolicyService {

    @Value("${youth-policy.apikey}")
    private String apiKey;

    @Value("${youth-policy.default-url}")
    private String url;

    private final PolicyRepository repository;

    @Override
    public List<PolicyDto> getPolicies() {
        List<PolicyDto> allPolicies = new ArrayList<>();
        int pageIndex = 1;
        int totalCnt = 0;

        do {
            // URL, Parameter 빌드
            int MAX_DISPLAY = 100;
            String uri = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("pageIndex", pageIndex)
                    .queryParam("display", MAX_DISPLAY)
                    .queryParam("openApiVlak", apiKey)
                    .queryParam("srchPolyBizSecd", SEOUL.getCode())
                    .toUriString();

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response;
            try {
                response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            } catch (RestClientException e) {
                log.error(" >>> ❌ API 호출 오류 및 재시도 실행: {}", e.getMessage());
                try {
                    Thread.sleep(5000); // 5s 대기 후 실행
                    response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
                } catch (Exception ex) {
                    log.error(" >>> ❌ API 재호출 실패: {}", ex.getMessage());
                    return new ArrayList<>(); // TODO: API 호출 실패 Exception 처리
                }
            }

            // XML -> JSON
            String jsonResponse = XML.toJSONObject(Objects.requireNonNull(response.getBody()))
                    .toString();
            JSONObject root = new JSONObject(jsonResponse);
            JSONObject youthPolicyList = root.optJSONObject("youthPolicyList");

            // 총 데이터 수 확인
            if (totalCnt == 0 && youthPolicyList != null) {
                totalCnt = youthPolicyList.optInt("totalCnt");
            }

            List<PolicyDto> policies = parsePolicyData(jsonResponse);
            allPolicies.addAll(policies);

            pageIndex++;
        } while (allPolicies.size() < totalCnt);

        return allPolicies;
    }

    private List<PolicyDto> parsePolicyData(String response) {
        JSONObject root = new JSONObject(response);
        JSONObject youthPolicyList = root.optJSONObject("youthPolicyList");
        if (youthPolicyList == null) {
            log.error(" >>> ❌ 'youthPolicyList' 객체를 찾을 수 없습니다.");
            return new ArrayList<>(); // TODO: 실패 Exception 처리
        }

        JSONArray youthPolicies = youthPolicyList.optJSONArray("youthPolicy");
        if (youthPolicies == null) {
            log.error(" >>> ❌ 'youthPolicyList' 객체에서 'youthPolicy' 배열을 찾을 수 없습니다.");
            return new ArrayList<>(); // TODO: 실패 Exception 처리
        }

        List<PolicyDto> data = new ArrayList<>();
        for (int item = 0; item < youthPolicies.length(); item++) {
            JSONObject policy = youthPolicies.optJSONObject(item);

            String polyBizSecd = policy.optString("polyBizSecd");
            String prdRpttSecd = policy.optString("prdRpttSecd");
            String districtCode = "26";
            if (polyBizSecd.length() > 9) {
                districtCode=polyBizSecd.substring(polyBizSecd.length() - 2);
            }

            String rqutPrdCn = policy.optString("rqutPrdCn");
            Map<String, String> dates = ExtractFormatter.extractDates(rqutPrdCn);

            Map<String, Integer> ageRange = AgeInfoFormatter.extractAgeRange(
                    policy.optString("ageInfo"));

            PolicyDto resDto = PolicyDto.builder()
                    .policyCode(policy.optString("bizId"))
                    .categoryCode(policy.optString("polyRlmCd"))
                    .districtCode(districtCode)
                    .policyName(policy.optString("polyBizSjnm"))
                    .policyContent(policy.optString("polyItcnCn"))
                    .supportContent(policy.optString("sporCn"))
                    .prdRpttSecd(
                            PeriodFormatter.formatPrdRpttSecd(prdRpttSecd)) // 사업 신청 기간 반복 구분 내용
                    .schedule(rqutPrdCn) // TODO: 단어 필터링 지속적인 확인 필요. 추후 삭제될 컬럼.
                    .policyStartDate(dates.get("policyStartDate"))
                    .policyEndDate(dates.get("policyEndDate"))
                    .ageInfo(policy.optString("ageInfo")) // TODO: 단어 필터링 지속적인 확인 필요. 추후 삭제될 컬럼.
                    .minAge(ageRange.get("minAge"))
                    .maxAge(ageRange.get("maxAge"))
                    .majorRqisCn(policy.optString("majorRqisCn"))
                    .empmSttsCn(policy.optString("empmSttsCn"))
                    .splzRlmRqisCn(policy.optString("splzRlmRqisCn"))
                    .accrRqisCn(policy.optString("accrRqisCn"))
                    .prcpCn(policy.optString("prcpCn"))
                    .aditRscn(policy.optString("aditRscn"))
                    .prcpLmttTrgtCn(policy.optString("prcpLmttTrgtCn"))
                    .rqutProcCn(policy.optString("rqutProcCn"))
                    .pstnPaprCn(policy.optString("pstnPaprCn"))
                    .jdgnPresCn(policy.optString("jdgnPresCn"))
                    .rqutUrla(policy.optString("rqutUrla"))
                    .rfcSiteUrla1(policy.optString("rfcSiteUrla1"))
                    .rfcSiteUrla2(policy.optString("rfcSiteUrla2"))
                    .mngtMson(policy.optString("mngtMson"))
                    .mngtMrofCherCn(policy.optString("mngtMrofCherCn"))
                    .cherCtpcCn(policy.optString("cherCtpcCn"))
                    .cnsgNmor(policy.optString("cnsgNmor"))
                    .tintCherCn(policy.optString("tintCherCn"))
                    .tintCherCtpcCn(policy.optString("tintCherCtpcCn"))
                    .etct(policy.optString("etct"))
                    .build();
            data.add(resDto);
        }
        return data;
    }

    @Override
    public void savePolicies(List<PolicyDto> policies) {
        if (policies.isEmpty()) {
            log.info(" >>> ❌ 저장할 데이터가 없습니다.");
        }
        for (PolicyDto dto : policies) {
            try {
                Optional<Policy> existingPolicy = repository.findById(dto.getPolicyCode());

                if (existingPolicy.isPresent()) {
                    log.info(" >>> 🔄 중복된 정책 데이터 발견 (저장되지 않음): {}", dto.getPolicyCode());
                    continue;
                }
                Policy policy = Policy.builder()
                        .policyCode(dto.getPolicyCode())
                        .categoryCode(dto.getCategoryCode())
                        .districtCode(dto.getDistrictCode())
                        .policyName(dto.getPolicyName())
                        .policyContent(dto.getPolicyContent())
                        .supportContent(dto.getSupportContent())
                        .prdRpttSecd(dto.getPrdRpttSecd())
                        .schedule(dto.getSchedule()) // TODO: 단어 필터링 지속적인 확인 필요. 추후 삭제될 컬럼.
                        .policyStartDate(dto.getPolicyStartDate())
                        .policyEndDate(dto.getPolicyEndDate())
                        .ageInfo(dto.getAgeInfo()) // TODO: 단어 필터링 지속적인 확인 필요. 추후 삭제될 컬럼.
                        .minAge(dto.getMinAge())
                        .maxAge(dto.getMaxAge())
                        .majorRqisCn(dto.getMajorRqisCn())
                        .empmSttsCn(dto.getEmpmSttsCn())
                        .splzRlmRqisCn(dto.getSplzRlmRqisCn())
                        .accrRqisCn(dto.getAccrRqisCn())
                        .prcpCn(dto.getPrcpCn())
                        .aditRscn(dto.getAditRscn())
                        .prcpLmttTrgtCn(dto.getPrcpLmttTrgtCn())
                        .rqutProcCn(dto.getRqutProcCn())
                        .pstnPaprCn(dto.getPstnPaprCn())
                        .jdgnPresCn(dto.getJdgnPresCn())
                        .rqutUrla(dto.getRqutUrla())
                        .rfcSiteUrla1(dto.getRfcSiteUrla1())
                        .rfcSiteUrla2(dto.getRfcSiteUrla2())
                        .mngtMson(dto.getMngtMson())
                        .mngtMrofCherCn(dto.getMngtMrofCherCn())
                        .cherCtpcCn(dto.getCherCtpcCn())
                        .cnsgNmor(dto.getCnsgNmor())
                        .tintCherCn(dto.getTintCherCn())
                        .tintCherCtpcCn(dto.getTintCherCtpcCn())
                        .etct(dto.getEtct())
                        .build();
                repository.save(policy);
            } catch (Exception e) {
                log.error(" >>> ❌ 데이터 저장 중 오류 발생: {}", e.getMessage());
            }
        }
        log.info(" >>> ✅ 청년 정책 데이터가 데이터베이스에 저장되었습니다.");
    }
}
