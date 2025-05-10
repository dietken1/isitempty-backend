package com.isitempty.backend.oauthlogin.api.controller.user;

import com.isitempty.backend.oauthlogin.api.entity.user.User;
import com.isitempty.backend.oauthlogin.api.form.UserCreateForm;
import com.isitempty.backend.oauthlogin.api.form.UserResponse;
import com.isitempty.backend.oauthlogin.api.form.UserUpdateForm;
import com.isitempty.backend.oauthlogin.api.service.UserService;
import com.isitempty.backend.oauthlogin.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

//    @GetMapping
//    public ApiResponse getUser() {
//        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//
//        User user = userService.getUser(principal.getUsername());
//
//        return ApiResponse.success("user", user);
//    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")  // 관리자만 모든 사용자 목록을 볼 수 있음
    public ResponseEntity<?> getUser() {
        try {
            // 모든 사용자 목록 조회
            List<User> users = userService.getAllUsers();
            
            // UserResponse 목록으로 변환
            List<UserResponse> userResponses = users.stream()
                .map(user -> new UserResponse(
                    user.getUserId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRoleType().getCode().replace("ROLE_", "")  // ROLE_ 접두사 제거
                ))
                .collect(Collectors.toList());
            
            // users 배열을 직접 반환
            return ResponseEntity.ok(userResponses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 정보 조회 실패: " + e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody UserCreateForm userCreateForm, BindingResult bindingResult) {
        // 유효성 검사 오류 처리
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        // 비밀번호 일치 확인
        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            return ResponseEntity.badRequest().body("비밀번호가 일치하지 않습니다.");
        }

        try {
            // 사용자 생성
            userService.createUser(userCreateForm.getUsername(),
                    userCreateForm.getName(),
                    userCreateForm.getEmail(),
                    userCreateForm.getPassword1());
            return ResponseEntity.ok("회원가입 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원가입 실패: " + e.getMessage());
        }
    }

    @PostMapping("/createadmin")
    public ResponseEntity<?> SignupAdmin(@Valid @RequestBody UserCreateForm userCreateForm, BindingResult bindingResult) {
        // 유효성 검사 오류 처리
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        // 비밀번호 일치 확인
        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            return ResponseEntity.badRequest().body("비밀번호가 일치하지 않습니다.");
        }

        try {
            // 사용자 생성
            userService.createAdmin(userCreateForm.getUsername(),
                    userCreateForm.getName(),
                    userCreateForm.getEmail(),
                    userCreateForm.getPassword1());
            return ResponseEntity.ok("회원가입 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원가입 실패: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout() {
        // JWT는 서버에서 세션을 관리하지 않으므로 클라이언트에서 토큰 삭제를 처리
        // 서버에서는 추가적인 블랙리스트 처리(선택 사항)를 구현할 수 있음
        return ResponseEntity.ok("로그아웃 성공");
    }

    //delete, update 구현 필요
    // 사용자 정보 수정
    @PutMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> update(@Valid @RequestBody UserUpdateForm userUpdateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        try {
            String userId = getCurrentUserId();
            User user = userService.getUser(userId);
            userService.updateUser(user.getUserId(), userUpdateForm.getUsername(), userUpdateForm.getEmail(), userUpdateForm.getPassword());
            return ResponseEntity.ok("정보 수정 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("정보 수정 실패: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> delete() {
        try {
            String username;
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                username = ((User) principal).getUsername();
            } else {
                username = principal.toString();
            }
            User user = userService.getUser(username);
            userService.deleteUser(user.getUsername());
            return ResponseEntity.ok("계정 삭제 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("계정 삭제 실패: " + e.getMessage());

        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser() {
        try {
            String userId = getCurrentUserId();
            User user = userService.getUser(userId);
            UserResponse userResponse = new UserResponse(
                    user.getUserId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRoleType().getCode().replace("ROLE_", "")  // ROLE_ 접두사 제거
            );
            
            // user 키 없이 직접 반환
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 정보 조회 실패: " + e.getMessage());
        }
    }

    private String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }
}