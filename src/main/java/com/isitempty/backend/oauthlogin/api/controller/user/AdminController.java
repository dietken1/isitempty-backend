package com.isitempty.backend.oauthlogin.api.controller.user;

import com.isitempty.backend.oauthlogin.api.entity.user.User;
import com.isitempty.backend.oauthlogin.api.form.UserResponse;
import com.isitempty.backend.oauthlogin.api.form.UserUpdateForm;
import com.isitempty.backend.oauthlogin.api.service.UserService;
import com.isitempty.backend.oauthlogin.oauth.entity.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final UserService userService;
    
    @PostMapping("/check")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> check() {
        return ResponseEntity.ok("admin 인증 성공");
    }
    
    /**
     * 관리자 권한으로 모든 사용자 목록을 조회합니다.
     */
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            
            List<UserResponse> userResponses = users.stream()
                .map(user -> new UserResponse(
                    user.getUserId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRoleType().getCode().replace("ROLE_", "")
                ))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(userResponses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("사용자 목록 조회 실패: " + e.getMessage());
        }
    }
    
    /**
     * 관리자 권한으로 특정 사용자 정보를 조회합니다.
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable String userId) {
        try {
            User user = userService.getUser(userId);
            UserResponse userResponse = new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoleType().getCode().replace("ROLE_", "")
            );
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("사용자 조회 실패: " + e.getMessage());
        }
    }
    
    /**
     * 관리자 권한으로 사용자 정보를 수정합니다.
     * 사용자명, 이메일, 비밀번호, 역할을 변경할 수 있습니다.
     */
    @PutMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateUser(
            @PathVariable String userId,
            @RequestBody Map<String, Object> updateData) {
        try {
            String username = (String) updateData.get("username");
            String email = (String) updateData.get("email");
            String password = (String) updateData.get("password");
            String roleType = (String) updateData.get("roleType");
            
            RoleType newRoleType = null;
            if (roleType != null) {
                if (roleType.equals("ADMIN")) {
                    newRoleType = RoleType.ADMIN;
                } else if (roleType.equals("USER")) {
                    newRoleType = RoleType.USER;
                } else {
                    return ResponseEntity.badRequest().body("유효하지 않은 역할 유형입니다. 'ADMIN' 또는 'USER'만 허용됩니다.");
                }
            }
            
            User updatedUser = userService.adminUpdateUser(userId, username, email, password, newRoleType);
            
            UserResponse userResponse = new UserResponse(
                updatedUser.getUserId(),
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getRoleType().getCode().replace("ROLE_", "")
            );
            
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("사용자 정보 수정 실패: " + e.getMessage());
        }
    }
    
    /**
     * 관리자 권한으로 사용자의 역할만 변경합니다.
     */
    @PatchMapping("/users/{userId}/role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateUserRole(
            @PathVariable String userId,
            @RequestBody Map<String, String> roleData) {
        try {
            String roleType = roleData.get("roleType");
            if (roleType == null) {
                return ResponseEntity.badRequest().body("역할 정보가 누락되었습니다.");
            }
            
            RoleType newRoleType;
            if (roleType.equals("ADMIN")) {
                newRoleType = RoleType.ADMIN;
            } else if (roleType.equals("USER")) {
                newRoleType = RoleType.USER;
            } else {
                return ResponseEntity.badRequest().body("유효하지 않은 역할 유형입니다. 'ADMIN' 또는 'USER'만 허용됩니다.");
            }
            
            User updatedUser = userService.updateUserRole(userId, newRoleType);
            
            UserResponse userResponse = new UserResponse(
                updatedUser.getUserId(),
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getRoleType().getCode().replace("ROLE_", "")
            );
            
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("역할 변경 실패: " + e.getMessage());
        }
    }
    
    /**
     * 관리자 권한으로 사용자를 삭제합니다.
     */
    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok("사용자 삭제 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("사용자 삭제 실패: " + e.getMessage());
        }
    }
}
