package sandbox.apricot.scheduler;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sandbox.apricot.util.GeomFilterConverter;

@Component
@RequiredArgsConstructor
@Slf4j
public class UtilUpdateScheduler {

//  @Scheduled(fixedRate = 10000) // 테스트용 10초
  @Scheduled(cron = "0 15 4 1 * *") // 매달 1일 4시 15분 업데이트.
  public void schedule()
      throws UnsupportedEncodingException, InterruptedException, FileNotFoundException {
    log.info(" >>> ➡️ 멀티폴리곤(지역 그래프) 데이터 변환 시작");
    GeomFilterConverter.createMultipolygonRegion();

    log.info(" >>> ✅ 멀티폴리곤(지역 그래프) 데이터 변환 시작");
  }

}
