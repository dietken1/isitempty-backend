package com.isitempty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.isitempty")
public class IsitemptyApplication {

    public static void main(String[] args) {
        SpringApplication.run(IsitemptyApplication.class, args);
    }
} 