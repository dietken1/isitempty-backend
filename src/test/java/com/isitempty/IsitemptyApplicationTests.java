package com.isitempty;

import com.isitempty.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = IsitemptyApplication.class)
@ActiveProfiles({"test", "github"})
@EnableAutoConfiguration(exclude = {
    RedisAutoConfiguration.class
})
@Import(TestConfig.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.data.redis.enabled=false",
    "SEOUL_KEY=test-api-key",
    "ssh.enabled=false",
    "ssh.host=localhost",
    "ssh.port=22",
    "ssh.username=test",
    "ssh.password=test",
    "ssh.remote.host=localhost",
    "ssh.remote.port=3306",
    "ssh.local.port=3307"
})
class IsitemptyApplicationTests {

    @Test
    void contextLoads() {
        // 컨텍스트 로드 테스트
    }
} 