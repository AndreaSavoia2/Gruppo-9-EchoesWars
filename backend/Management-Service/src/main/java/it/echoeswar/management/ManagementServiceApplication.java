package it.echoeswar.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class ManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManagementServiceApplication.class, args);
    }

}
