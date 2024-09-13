package sandbox.apricot.artmuseum.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArtMuseumDTO {

  private String museumName; // 시설명
  private String museumType; // 미술관/박물관 분

  private String districtCode; // 지역구명 🎈
  private String address; // 소재지도로명주소

  private String operationTel; // 운영기관전화번호
  private String operationName; // 운영기관명

  private String wdsTme; // 평일관람시작시각
  private String wdeTme; // 평일관람종료시각
  private String hdsTme; // 공휴일관람시작시각
  private String hdeTme; // 공휴일관람종료시각
  private String hdyInf; // 휴관정보
  private String adtFee; // 어른관람료
  private String yotFee; // 청소년관람료
  private String chdFee; // 어린이관람료

  private String mngTel; // 관리기관전화번호
  private String mngNam; // 관리기관명
  private String regDat; // 데이터기준일자
}
