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
        return ResponseEntity.ok("admin 인증 성공");
    }
}
