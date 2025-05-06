package com.isitempty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.isitempty", "com.isitempty.config", "com.isitempty.backend"})

@EnableJpaRepositories(basePackages = {
        "com.isitempty.backend.review.repository",
        "com.isitempty.backend.parkinglot.repository",
        "com.isitempty.backend.oauthlogin.api.repository.user",
        "com.isitempty.backend.camera.repository"
})
@EntityScan(basePackages = {
        "com.isitempty.backend.review.entity",
        "com.isitempty.backend.parkinglot.entity",
        "com.isitempty.backend.oauthlogin.api.entity.user",
        "com.isitempty.backend.camera.entity"
})
public class IsitemptyApplication {
    
    private static final Logger log = LoggerFactory.getLogger(IsitemptyApplication.class);

    public static void main(String[] args) {
        // Spring 애플리케이션 실행
        ConfigurableApplicationContext context = SpringApplication.run(IsitemptyApplication.class, args);
        
        // 환경 변수 확인용 로그
        Environment env = context.getEnvironment();
        log.info("=== 환경 변수 테스트 ===");
        log.info("SSH_HOST: {}", env.getProperty("ssh.host"));
        log.info("SSH_USERNAME: {}", env.getProperty("ssh.username"));
        log.info("SSH_PASSWORD: {}", (env.getProperty("ssh.password") != null ? "설정됨" : "설정되지 않음"));
        log.info("REDIS_PASSWORD: {}", (env.getProperty("spring.data.redis.password") != null ? "설정됨" : "설정되지 않음"));
        log.info("========================");
    }
}