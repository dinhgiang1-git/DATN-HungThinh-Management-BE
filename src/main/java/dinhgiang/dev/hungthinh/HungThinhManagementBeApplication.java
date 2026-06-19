package dinhgiang.dev.hungthinh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HungThinhManagementBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(HungThinhManagementBeApplication.class, args);
    }

}
