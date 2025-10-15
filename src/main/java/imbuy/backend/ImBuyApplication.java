package imbuy.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ImBuyApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImBuyApplication.class, args);
    }
}
