package com.isitempty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.isitempty", "com.isitempty.config", "com.isitempty.backend"})
public class IsitemptyApplication {

    public static void main(String[] args) {
        // SSH 터널링 코드 제거 또는 주석 처리
        SpringApplication.run(IsitemptyApplication.class, args);
    }
} 