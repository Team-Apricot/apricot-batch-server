package sandbox.apricot.artmuseum.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.json.simple.JSONObject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired
  RestTemplate restTemplate;

  private final ArtMuseumRepository repository;

    @Override
  public List<ArtMuseumDTO> getAllArtMuseums()
      throws UnsupportedEncodingException, InterruptedException {
    List<ArtMuseumDTO> allArtMuseums = new ArrayList<>();
// 1. GeomFilter를 활용한 방식
    // MultiPolygon Enum을 통해 모든 지역구 uri별로 query 날리기.
    for (MultiPolygonRegion multipolygon : MultiPolygonRegion.values()) {

      // uri 변환시 빈 칸으로 인해 종종 error가 생기는 듯 해서 StringBuilder로 확인하기.
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
      try {
        response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
      } catch (RestClientException e) {
        log.error(" >>> ❌ API 호출 오류 및 재시도 실행: {}", e.getMessage());
        try {
          Thread.sleep(5000); // 5s 대기 후 실행
          response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        } catch (Exception ex) {
          log.error(" >>> ❌ API 재호출 실패: {}", ex.getMessage());
          continue;
//          return new ArrayList<>(); // TODO: API 호출 실패 Exception 처리
        }
        continue;
      }
      // response.getBody()로부터 받은 JSON 문자열
      String responseBody = response.getBody();

      // 문자열을 JSON 객체로 변환
      JSONObject jsonObject = new JSONObject(responseBody);
      List<ArtMuseumDTO> artMuseumList = parseArtMuseum(jsonObject);
      allArtMuseums.addAll(artMuseumList);
    }
    // 2. attrFilter의 단일 검색 방식을 이용한 방법(단일 검색시에만 GeomFilter가 필수가 아님)
    // 읍면동 코드를 모두 조회하여 확인 가능하다.(서울은 472개) 전국 5020개
    // 1. 읍면동 코드 => 시작코드가 무엇인지에 따라 분리 가능.
    return allArtMuseums;
  }
  // 결과 데이터를 파싱하여 DTO형식으로 바꿔주는 메서드
  private List<ArtMuseumDTO> parseArtMuseum(JSONObject jsonObject) {

    List<ArtMuseumDTO> data = new ArrayList<>();

    try {
// 원하는 값 추출하기
      JSONObject responseJson = jsonObject.optJSONObject("response");
      JSONObject resultJson = responseJson.optJSONObject("result");
      JSONObject featureCollection = resultJson.optJSONObject("featureCollection");
      JSONArray features = featureCollection.optJSONArray("features");

      for (int i = 0; i < features.length(); i++) {
        JSONObject feature = features.optJSONObject(i);
        JSONObject properties = feature.optJSONObject("properties");

        Pattern pattern = Pattern.compile("([가-힣]+[구|군])");
        Matcher matcher = pattern.matcher(properties.optString("new_adr"));
        String new_adr="";
        if (matcher.find()) {
          new_adr=matcher.group(1);
        }

        ArtMuseumDTO resultDTO = ArtMuseumDTO.builder()
                .mus_nam(properties.optString("mus_nam"))        // 시설명
                .mus_typ(properties.optString("mus_typ"))        // 박물관미술관구분
                .con_nam(new_adr)        // 지역구명 🎈
                .new_adr(properties.optString("new_adr"))        // 소재지도로명주소
                .opr_tel(properties.optString("opr_tel"))        // 운영기관전화번호
                .opr_nam(properties.optString("opr_nam"))        // 운영기관명
                .wds_tme(properties.optString("wds_tme"))        // 평일관람시작시각
                .wde_tme(properties.optString("wde_tme"))        // 평일관람종료시각
                .hds_tme(properties.optString("hds_tme"))        // 공휴일관람시작시각
                .hde_tme(properties.optString("hde_tme"))        // 공휴일관람종료시각
                .hdy_inf(properties.optString("hdy_inf"))        // 휴관정보
                .adt_fee(properties.optString("adt_fee"))        // 어른관람료
                .yot_fee(properties.optString("yot_fee"))        // 청소년관람료
                .chd_fee(properties.optString("chd_fee"))        // 어린이관람료
                .mng_tel(properties.optString("mng_tel"))        // 관리기관전화번호
                .mng_nam(properties.optString("mng_nam"))        // 관리기관명
                .reg_dat(properties.optString("reg_dat"))        // 데이터기준일자
                .build();
      data.add(resultDTO);
      }
    } catch (Exception e) {
      e.getMessage();
    }

    return data;
  }

  // API를 통해 받아온 정보를 ArtMuseumDTO객체로 받고, Entity로 저장한 후 Database에 저장하는 메서드
  @Override
  public void saveArtMuseum(List<ArtMuseumDTO> artMuseums) {
    for (ArtMuseumDTO dto : artMuseums) {
      try {
        Optional<ArtMuseum> exist = repository.findByArtMuseumCode(dto.getMus_nam());

        if (exist.isPresent()) {
          log.info(" >>> 🔄 중복된 데이터 발견 (저장되지 않음): {}", dto.getMng_nam());
          continue;
        }
        ArtMuseum artMuseum = ArtMuseum.builder()
                .mus_nam(dto.getMus_nam())        // 시설명
                .mus_typ(dto.getMus_typ())        // 박물관미술관구분
                .con_nam(dto.getCon_nam())        // 지역구명 🎈
                .new_adr(dto.getNew_adr())        // 소재지도로명주소
                .opr_tel(dto.getOpr_tel())        // 운영기관전화번호
                .opr_nam(dto.getOpr_nam())        // 운영기관명
                .wds_tme(dto.getWds_tme())        // 평일관람시작시각
                .wde_tme(dto.getWde_tme())        // 평일관람종료시각
                .hds_tme(dto.getHds_tme())        // 공휴일관람시작시각
                .hde_tme(dto.getHde_tme())        // 공휴일관람종료시각
                .hdy_inf(dto.getHdy_inf())        // 휴관정보
                .adt_fee(dto.getAdt_fee())        // 어른관람료
                .yot_fee(dto.getYot_fee())        // 청소년관람료
                .chd_fee(dto.getChd_fee())        // 어린이관람료
                .mng_tel(dto.getMng_tel())        // 관리기관전화번호
                .mng_nam(dto.getMng_nam())        // 관리기관명
                .reg_dat(dto.getReg_dat())        // 데이터기준일자
                .build();
        repository.save(artMuseum);
      } catch (Exception e) {
        log.error(" >>> ❌ 데이터 저장 중 오류 발생: {}", e.getMessage());
      }
    }
    log.info(" >>> ✅ 박물관/미술관 데이터가 데이터베이스에 저장되었습니다.");
  }
}

