package com.panda.salon_mgt_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SalonMgtBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalonMgtBackendApplication.class, args);
    }

}
