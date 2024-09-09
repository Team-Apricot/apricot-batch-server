package sandbox.apricot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApricotBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApricotBatchApplication.class, args);
    }

}
