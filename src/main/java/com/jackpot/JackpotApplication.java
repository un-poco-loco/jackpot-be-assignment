package com.jackpot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Main Spring Boot application class for Jackpot Service.
 * Manages jackpot contributions and rewards for a betting system.
 */
@SpringBootApplication
@EnableKafka
public class JackpotApplication {

    public static void main(String[] args) {
        SpringApplication.run(JackpotApplication.class, args);
    }
}
