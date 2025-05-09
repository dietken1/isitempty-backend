package com.isitempty.backend.oauthlogin.api.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    @PostMapping("/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> check() {
        // JWT는 서버에서 세션을 관리하지 않으므로 클라이언트에서 토큰 삭제를 처리
        // 서버에서는 추가적인 블랙리스트 처리(선택 사항)를 구현할 수 있음
        return ResponseEntity.ok("admin 인증 성공");
    }
}
