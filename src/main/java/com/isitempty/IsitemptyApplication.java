package com.isitempty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.isitempty", "com.isitempty.config", "com.isitempty.backend"})
public class IsitemptyApplication {

    public static void main(String[] args) {
        // Spring 애플리케이션 실행
        ConfigurableApplicationContext context = SpringApplication.run(IsitemptyApplication.class, args);
        
        // 환경 변수 확인용 로그
        Environment env = context.getEnvironment();
        System.out.println("=== 환경 변수 테스트 ===");
        System.out.println("SSH_HOST: " + env.getProperty("ssh.host"));
        System.out.println("SSH_USERNAME: " + env.getProperty("ssh.username"));
        System.out.println("SSH_PASSWORD: " + (env.getProperty("ssh.password") != null ? "설정됨" : "설정되지 않음"));
        System.out.println("REDIS_PASSWORD: " + (env.getProperty("spring.data.redis.password") != null ? "설정됨" : "설정되지 않음"));
        System.out.println("========================");
    }
}