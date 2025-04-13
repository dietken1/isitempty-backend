package com.isitempty.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class EnvTest2 {

    @Bean
    public CommandLineRunner testEnvMapping() {
        return args -> {
            System.out.println("=== 환경 변수 매핑 테스트 ===");
            System.out.println("SSH_HOST 환경 변수: " + System.getenv("SSH_HOST"));
            System.out.println("SSH_USERNAME 환경 변수: " + System.getenv("SSH_USERNAME"));
            System.out.println("SSH_PASSWORD 환경 변수: " + (System.getenv("SSH_PASSWORD") != null ? "설정됨" : "설정되지 않음"));
            System.out.println("========================");
        };
    }
} 