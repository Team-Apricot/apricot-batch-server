package sandbox.apricot.scheduler;


import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sandbox.apricot.artmuseum.dto.response.ArtMuseumDTO;
import sandbox.apricot.artmuseum.service.ArtMuseumService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArtMuseumScheduler {

  private final ArtMuseumService artMuseumService;

  //  @Scheduled(fixedRate = 60000) // 테스트 용도 1분, TODO: 프로젝트 배포 시점 삭제
  @Scheduled(cron = "0 5 4 * * MON-FRI") // 주중(월요일 ~ 금요일) 새벽 4시 5분에 실행
  public void schedule() throws UnsupportedEncodingException, InterruptedException {
    log.info(" >>> 🔄 미술관/박물관 데이터 수집 시작");

    List<ArtMuseumDTO> artMuseumDTOS = artMuseumService.getAllArtMuseums();

    if (artMuseumDTOS.isEmpty()) {
      log.info(" >>> 📉 저장할 새로운 데이터가 없습니다.");
    } else {
      log.info(" >>> 📈 {}개의 미술관/박물관 데이터를 수집하였습니다.", artMuseumDTOS.size());

      // 미술관/박물관 데이터 저장
      artMuseumService.saveArtMuseum(artMuseumDTOS);

      log.info(" >>> ✅ 미술관/박물관 데이터 수집 및 데이터베이스 저장 완료 - {}", new Date());
    }
  }


}
