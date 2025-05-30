package com.isitempty.config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev"})  // 개발환경에서는 비활성화
@ConditionalOnProperty(name = "ssh.enabled", havingValue = "true", matchIfMissing = false)  // 명시적으로 활성화 필요
public class SshTunnelConfig {
    
    private static final Logger log = LoggerFactory.getLogger(SshTunnelConfig.class);

    @Value("${ssh.host:223.130.134.121}")
    private String sshHost;

    @Value("${ssh.port:22}")
    private int sshPort;

    @Value("${ssh.username:root}")
    private String sshUsername;

    @Value("${ssh.password:}")
    private String sshPassword;

    @Value("${ssh.remote.host:localhost}")
    private String remoteHost;

    @Value("${ssh.remote.port:3306}")
    private int remotePort;

    @Value("${ssh.local.port:3307}")
    private int localPort;

    private Session session;

    @PostConstruct
    public void init() {
        try {
            log.info("SSH 터널링 설정 시작...");
            log.info("SSH 호스트: {}", sshHost);
            log.info("SSH 사용자: {}", sshUsername);
            log.info("SSH 비밀번호 설정됨: {}", (sshPassword != null && !sshPassword.isEmpty()));
            
            JSch jsch = new JSch();
            session = jsch.getSession(sshUsername, sshHost, sshPort);
            
            if (sshPassword != null && !sshPassword.isEmpty()) {
                session.setPassword(sshPassword);
            } else {
                log.warn("SSH 비밀번호가 설정되지 않았습니다!");
            }
            
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            
            log.info("SSH 연결 시도 중...");
            session.connect();
            log.info("SSH 연결 성공!");
            
            log.info("포트 포워딩 설정 중: {} -> {}:{}", localPort, remoteHost, remotePort);
            session.setPortForwardingL(localPort, remoteHost, remotePort);
            
            log.info("SSH 터널 설정 완료: localhost:{} -> {}:{}", localPort, remoteHost, remotePort);
        } catch (Exception e) {
            log.error("SSH 터널링 설정 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            log.info("SSH 터널 연결 종료");
        }
    }
} 