package com.isitempty.backend.oauthlogin.api.service;

import com.isitempty.backend.oauthlogin.api.entity.user.User;
import com.isitempty.backend.oauthlogin.api.repository.user.UserRepository;
import com.isitempty.backend.oauthlogin.oauth.entity.ProviderType;
import com.isitempty.backend.oauthlogin.oauth.entity.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

    public User updateUser(String userId, String username, String email, String password) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        if (username != null && !username.isEmpty()) {
            user.setUsername(username);
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
        user.setModifiedAt(LocalDateTime.now());
        this.userRepository.save(user);
        return user;
    }

    /**
     * 사용자의 역할(RoleType)을 변경합니다.
     * 관리자만 사용할 수 있는 메서드입니다.
     * 
     * @param userId 역할을 변경할 사용자의 ID
     * @param roleType 설정할 역할 (USER 또는 ADMIN)
     * @return 업데이트된 User 객체
     */
    public User updateUserRole(String userId, RoleType roleType) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        
        user.setRoleType(roleType);
        user.setModifiedAt(LocalDateTime.now());
        this.userRepository.save(user);
        return user;
    }

    /**
     * 관리자가 사용자 정보를 업데이트합니다.
     * 일반 updateUser와 달리 관리자 권한으로 실행됩니다.
     *
     * @param userId 업데이트할 사용자의 ID
     * @param username 새 사용자명 (null이면 변경 안함)
     * @param email 새 이메일 (null이면 변경 안함)
     * @param password 새 비밀번호 (null이면 변경 안함)
     * @param roleType 새 역할 (null이면 변경 안함)
     * @return 업데이트된 User 객체
     */
    public User adminUpdateUser(String userId, String username, String email, String password, RoleType roleType) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        
        if (username != null && !username.isEmpty()) {
            user.setUsername(username);
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
        
        if (roleType != null) {
            user.setRoleType(roleType);
        }
        
        user.setModifiedAt(LocalDateTime.now());
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

    /**
     * 모든 사용자 목록을 조회합니다.
     * @return 사용자 목록
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}