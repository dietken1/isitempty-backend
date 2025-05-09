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

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse getUser() {
        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userService.getUser(principal.getUsername());

        return ApiResponse.success("user", user);
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
            // Principal에서 username 추출
            String username;
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                username = ((User) principal).getUsername();
            } else {
                username = principal.toString(); // JWT에서 username이 String인 경우
            }
            User user = userService.getUser(username); // UserService에서 사용자 조회
            userService.updateUser(user.getUsername(), userUpdateForm.getEmail(), userUpdateForm.getPassword());
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
            return ResponseEntity.ok(new UserResponse(user.getUserId(), user.getUsername(), user.getEmail()));
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