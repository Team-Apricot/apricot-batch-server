package sandbox.apricot.artmuseum.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Builder
@Table(name = "art_museum_detail")
@AllArgsConstructor
public class ArtMuseum {

  @Id
  private String mus_nam; // 시설명
  private String mus_typ; // 박물관미술관구분

  private String con_nam; // 지역구명 🎈
  private String new_adr; // 소재지도로명주소

  private String opr_tel; // 운영기관전화번호
  private String opr_nam; // 운영기관명

  private String wds_tme; // 평일관람시작시각
  private String wde_tme; // 평일관람종료시각
  private String hds_tme; // 공휴일관람시작시각
  private String hde_tme; // 공휴일관람종료시각
  private String hdy_inf; // 휴관정보
  private String adt_fee; // 어른관람료
  private String yot_fee; // 청소년관람료
  private String chd_fee; // 어린이관람료

  private String mng_tel; // 관리기관전화번호
  private String mng_nam; // 관리기관명
  private String reg_dat; // 데이터기준일자

  private String artMuseumCode; // 새로운 필드 추가
}
