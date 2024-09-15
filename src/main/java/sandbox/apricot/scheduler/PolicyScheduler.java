package sandbox.apricot.scheduler;

import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sandbox.apricot.youth.dto.response.PolicyDto;
import sandbox.apricot.youth.service.PolicyService;

@Component
@RequiredArgsConstructor
@Slf4j
public class PolicyScheduler {

    private final PolicyService service;

    // @Scheduled(fixedRate = 60000) // 테스트 용도 1분, TODO: 프로젝트 배포 시점 삭제
    @Scheduled(cron = "0 0 4 * * MON-FRI") // 주중(월요일 ~ 금요일) 새벽 4시에 실행
    public void schedule() {
        log.info(" >>> 🔄 청년 정책 데이터 수집 시작");

        List<PolicyDto> policies = service.getPolicies();

        if (policies.isEmpty()) {
            log.info(" >>> 📉 저장할 새로운 데이터가 없습니다.");
        } else {
            log.info(" >>> 📈 {}개의 정책 데이터를 수집하였습니다.", policies.size());

            // 정책 데이터 저장
            service.savePolicies(policies);

            log.info(" >>> ✅ 청년 정책 데이터 수집 및 데이터베이스 저장 완료 - {}", new Date());
        }
    }
}
