package com.isitempty.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class EnvTest {
    
    @Bean
    public CommandLineRunner testEnv(
            @Value("${ssh.host:not-set}") String sshHost,
            @Value("${ssh.username:not-set}") String sshUsername,
            @Value("${ssh.password:not-set}") String sshPassword) {
        return args -> {
            System.out.println("=== 환경 변수 테스트 ===");
            System.out.println("SSH_HOST: " + sshHost);
            System.out.println("SSH_USERNAME: " + sshUsername);
            System.out.println("SSH_PASSWORD: " + (sshPassword.equals("not-set") ? "not-set" : "설정됨"));
            System.out.println("========================");
        };
    }
} 