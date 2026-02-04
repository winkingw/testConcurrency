package com.utgaming.testconcurrency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
public class TestConcurrencyApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestConcurrencyApplication.class, args);
    }

}
