package sandbox.apricot.youth.service;

import java.util.List;
import sandbox.apricot.youth.dto.response.PolicyDto;

public interface PolicyService {

    /**
     * OpenAPI 청년 정책 DTO 리스트로 반환합니다.
     *
     * @return 정책 데이터 리스트
     */
    List<PolicyDto> getPolicies();

    /**
     * 정책 데이터를 데이터베이스에 저장합니다.
     */
    void savePolicies(List<PolicyDto> policies);

}
