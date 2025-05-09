package com.isitempty.backend.oauthlogin.api.service;

import com.isitempty.backend.oauthlogin.api.entity.user.User;
import com.isitempty.backend.oauthlogin.api.repository.user.UserRepository;
import com.isitempty.backend.oauthlogin.oauth.entity.ProviderType;
import com.isitempty.backend.oauthlogin.oauth.entity.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUser(String userId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        return user;
    }

    public User createUser(String userId, String name, String email, String password) {
        if (userRepository.findByUserId(userId) != null) {
            throw new RuntimeException("이미 존재하는 사용자 ID입니다.");
        }
        if (userRepository.findByEmail(email) != null) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }
        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setUserId(userId);
        user.setUsername(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setProviderType(ProviderType.LOCAL);
        user.setRoleType(RoleType.USER);
        user.setEmailVerifiedYn("N"); // <-- 추가
        user.setCreatedAt(LocalDateTime.now());
        user.setModifiedAt(now);      // <-- 추가
        this.userRepository.save(user);
        return user;
    }

    public User createAdmin(String userId, String name, String email, String password) {
        if (userRepository.findByUserId(userId) != null) {
            throw new RuntimeException("이미 존재하는 사용자 ID입니다.");
        }
        if (userRepository.findByEmail(email) != null) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }
        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setUserId(userId);
        user.setUsername(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setProviderType(ProviderType.LOCAL);
        user.setRoleType(RoleType.ADMIN);
        user.setEmailVerifiedYn("N"); // <-- 추가
        user.setCreatedAt(LocalDateTime.now());
        user.setModifiedAt(now);      // <-- 추가
        this.userRepository.save(user);
        return user;
    }

    public User updateUser(String userId, String email, String password) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        if (email != null && !email.isEmpty()) {
            if (userRepository.findByEmail(email) != null && !user.getEmail().equals(email)) {
                throw new RuntimeException("이미 사용 중인 이메일입니다.");
            }
            user.setEmail(email);
        }
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        this.userRepository.save(user);
        return user;
    }

    public void deleteUser(String userId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        userRepository.delete(user);
    }
}