package sandbox.apricot.youth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PolicyDto {

    private String policyCode; // 정책 코드
    private String categoryCode; // 정책에 속하는 분야 코드
    private String districtCode; // 지역구
    private String policyName; // 정책명
    private String policyContent; // 정책 내용
    private String supportContent; // 지원 내용

    private String prdRpttSecd; // 사업 신청 기간 반복 구분 내용
    private String schedule; // TODO: 가공 후 삭제. 일정 -> 정책 시작, 마감일 가공 패턴 찾기
    private String policyStartDate; // 정책 시작일
    private String policyEndDate; // 정책 마감일

    private String ageInfo; // TODO: 가공 후 삭제. 연령 정보 가공 패턴 찾기
    private Integer minAge; // 최소 연령
    private Integer maxAge; // 최대 연령

    private String majorRqisCn; // 전공 요건 내용
    private String empmSttsCn; // 취업 상태 내용
    private String splzRlmRqisCn; // 특별 분야 내용
    private String accrRqisCn; // 학력 요건 내용
    private String prcpCn; // 거주지 및 소득 조건 내용
    private String aditRscn; // 추가 단서 사항 내용
    private String prcpLmttTrgtCn; // 참여 제한 대상 내용
    private String rqutProcCn; // 신청 절차 내용
    private String pstnPaprCn; // 제출 서류 내용
    private String jdgnPresCn; // 심사 발표 내용

    private String rqutUrla; // 신청 사이트 주소
    private String rfcSiteUrla1; // 참고 사이트 URL 주소1
    private String rfcSiteUrla2; // 참고 사이트 URL 주소2

    private String mngtMson; // 주관 부처명
    private String mngtMrofCherCn; // 주관 부처 담당자 이름
    private String cherCtpcCn; // 주관 부처 담당자 연락처

    private String cnsgNmor; // 운영 기관명
    private String tintCherCn; // 운영 기관 담당자 이름
    private String tintCherCtpcCn; // 운영 기관 담당자 연락처

    private String etct; // 기타 사항

}
