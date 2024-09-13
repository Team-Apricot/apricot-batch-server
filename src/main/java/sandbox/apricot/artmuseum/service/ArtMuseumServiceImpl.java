package sandbox.apricot.artmuseum.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import sandbox.apricot.artmuseum.dto.response.ArtMuseumDTO;
import sandbox.apricot.artmuseum.entity.ArtMuseum;
import sandbox.apricot.artmuseum.repository.ArtMuseumRepository;
import sandbox.apricot.util.MultiPolygonRegion;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ArtMuseumServiceImpl implements ArtMuseumService {

  @Value("${Art-Museum.apikey}")
  private String apiKey;

  @Value("${Art-Museum.default-url}")
  private String defaultUrl;

  private final ArtMuseumRepository repository;

  @Override
  public List<ArtMuseumDTO> getAllArtMuseums()
      throws UnsupportedEncodingException {
    List<ArtMuseumDTO> allArtMuseums = new ArrayList<>();
// 1. GeomFilter를 활용한 방식
    // MultiPolygon Enum의 값들 : 모든 지역구별 geomFilter를 적용하여 url 전송.
    for (MultiPolygonRegion multipolygon : MultiPolygonRegion.values()) {

      // uri 변환시 빈 칸 => %20 이 변화로 geomFilter값 오류가 생겨 StringBuilder로 String화 후 변환하지 않고 전송.
      StringBuilder uriBuilder = new StringBuilder(defaultUrl);
      uriBuilder.append("?request=").append(URLEncoder.encode("GetFeature", "UTF-8"));
      uriBuilder.append("&key=").append(URLEncoder.encode(apiKey, "UTF-8"));
      uriBuilder.append("&data=").append(URLEncoder.encode("LT_P_DGMUSEUMART", "UTF-8"));
      uriBuilder.append("&domain=").append(URLEncoder.encode("localhost", "UTF-8"));
      uriBuilder.append("&geomFilter=").append(multipolygon.getMultipolygon());
      String uri = uriBuilder.toString();

      RestTemplate restTemplate = new RestTemplate();
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<String> response;

      // API 호출 실패시 재시도
      try {
        response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
      } catch (RestClientException e) {
        log.error(" >>> ❌ {}의 API 호출 오류 및 재시도 실행: {}", multipolygon.getKorName(), e.getMessage());
        try {
          Thread.sleep(5000); // 5s 대기 후 실행
          response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        } catch (Exception ex) {
          log.error(" >>> ❌ {} API 재호출 실패: {}", multipolygon.getKorName(), ex.getMessage());
          continue;
        }
      }
      log.info(" >>> 📥 {} 의 데이터 수집 완료.", multipolygon.getKorName());
      // response.getBody()로부터 받은 JSON 문자열
      String responseBody = response.getBody();

      // 문자열을 JSON 객체로 변환
      JSONObject jsonObject = new JSONObject(responseBody);
      List<ArtMuseumDTO> artMuseumList = parseArtMuseum(jsonObject);
      allArtMuseums.addAll(artMuseumList);
    }

// TODO : geomfilter보다 더욱 정확한 방법 적용
//  2. attrFilter의 단일 검색 방식을 이용한 방법(단일 검색시에만 GeomFilter가 필수가 아님)
//  읍면동 코드를 모두 조회하여 확인 가능하다.(서울은 472개) 전국 5020개

    return allArtMuseums;
  }

  // 결과 데이터를 파싱하여 DTO형식으로 바꿔주는 메서드
  private List<ArtMuseumDTO> parseArtMuseum(JSONObject jsonObject) {

    // 미술관/박물관 데이터를 담을 ArrayList 생성
    List<ArtMuseumDTO> data = new ArrayList<>();

    try {
      // JSONObject에서 원하는 값 추출 (optJSONObject : 없으면 null이 아닌 ""를 return)
      JSONObject responseJson = jsonObject.optJSONObject("response");
      JSONObject resultJson = responseJson.optJSONObject("result");
      JSONObject featureCollection = resultJson.optJSONObject("featureCollection");
      JSONArray features = featureCollection.optJSONArray("features");

      for (int i = 0; i < features.length(); i++) {
        JSONObject feature = features.optJSONObject(i);
        JSONObject properties = feature.optJSONObject("properties");

        Pattern pattern = Pattern.compile("([가-힣]+[구|군])");
        Matcher matcher = pattern.matcher(properties.optString("new_adr"));
        String districtCode = "";
        if (matcher.find()) {
          districtCode = matcher.group(1);
        }

        ArtMuseumDTO resultDTO = ArtMuseumDTO.builder()
            .museumName(properties.optString("mus_nam"))          // 시설명
            .museumType(properties.optString("mus_typ"))          // 미술관/박물관 구분
            .districtCode(districtCode)                              // 지역구명 🆕
            .address(properties.optString("new_adr"))                // 소재지도로명주소
            .operationTel(properties.optString("opr_tel"))      // 운영기관전화번호
            .operationName(properties.optString("opr_nam"))    // 운영기관명
            .wdsTme(properties.optString("wds_tme"))                  // 평일관람시작시각
            .wdeTme(properties.optString("wde_tme"))                  // 평일관람종료시각
            .hdsTme(properties.optString("hds_tme"))                  // 공휴일관람시작시각
            .hdeTme(properties.optString("hde_tme"))                  // 공휴일관람종료시각
            .hdyInf(properties.optString("hdy_inf"))                  // 휴관정보
            .adtFee(properties.optString("adt_fee"))                  // 어른관람료
            .yotFee(properties.optString("yot_fee"))                  // 청소년관람료
            .chdFee(properties.optString("chd_fee"))                  // 어린이관람료
            .mngTel(properties.optString("mng_tel"))                  // 관리기관전화번호
            .mngNam(properties.optString("mng_nam"))                  // 관리기관명
            .regDat(properties.optString("reg_dat"))                  // 데이터기준일자
            .build();
        data.add(resultDTO);
      }
    } catch (Exception e) {
      e.getMessage();
    }

    return data;
  }

  // API를 통해 받아온 정보를 ArtMuseumDTO객체 형식으로 받고, Entity에 저장하고, Database에 저장하는 메서드
  @Override
  public void saveArtMuseum(List<ArtMuseumDTO> artMuseums) {
    for (ArtMuseumDTO dto : artMuseums) {
      try {
        Optional<ArtMuseum> exist = repository.findArtMuseumByMuseumName(dto.getMuseumName());

        if (exist.isPresent()) {
          log.info(" >>> 🔄 중복된 데이터 발견 (저장되지 않음): {}", dto.getMuseumName());
          continue;
        }
        ArtMuseum artMuseum = ArtMuseum.builder()
            .museumName(dto.getMuseumName())        // 시설명
            .museumType(dto.getMuseumType())        // 미술관/박물관 구분
            .districtCode(dto.getDistrictCode())    // 지역구명 🎈
            .address(dto.getAddress())              // 소재지도로명주소
            .operationTel(dto.getOperationTel())    // 운영기관전화번호
            .operationName(dto.getOperationName())  // 운영기관명
            .wdsTme(dto.getWdsTme())                // 평일관람시작시각
            .wdeTme(dto.getWdeTme())                // 평일관람종료시각
            .hdsTme(dto.getHdsTme())                // 공휴일관람시작시각
            .hdeTme(dto.getHdeTme())                // 공휴일관람종료시각
            .hdyInf(dto.getHdyInf())                // 휴관정보
            .adtFee(dto.getAdtFee())                // 어른관람료
            .yotFee(dto.getYotFee())                // 청소년관람료
            .chdFee(dto.getChdFee())                // 어린이관람료
            .mngTel(dto.getMngTel())                // 관리기관전화번호
            .mngNam(dto.getMngNam())                // 관리기관명
            .regDat(dto.getRegDat())                // 데이터기준일자
            .build();
        repository.save(artMuseum);
      } catch (Exception e) {
        log.error(" >>> ❌ 데이터 저장 중 오류 발생: {}", e.getMessage());
      }
    }
    log.info(" >>> ✅ 미술관/박물관 데이터가 데이터베이스에 저장되었습니다.");
  }
}

