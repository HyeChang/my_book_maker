package com.bookmark.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @GetMapping("/user")
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return null;
        }
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", principal.getAttribute("name"));
        userInfo.put("email", principal.getAttribute("email"));
        userInfo.put("picture", principal.getAttribute("picture"));
        userInfo.put("authenticated", true);
        
        return userInfo;
    }
    
    @GetMapping("/status")
    public Map<String, Object> getAuthStatus(Authentication authentication) {
        Map<String, Object> status = new HashMap<>();
        status.put("authenticated", authentication != null && authentication.isAuthenticated());
        return status;
    }
    
    /**
     * 로그아웃 처리
     * - 현재 인증 정보를 가져와서 세션을 무효화
     * - SecurityContextLogoutHandler를 사용하여 완전한 로그아웃 처리
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @return 로그아웃 성공 메시지
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        // 현재 인증 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            // 세션 무효화 및 보안 컨텍스트 클리어
            new SecurityContextLogoutHandler().logout(request, response, auth);
            log.info("User logged out successfully: {}", auth.getName());
        }
        
        // 로그아웃 성공 응답 반환
        Map<String, String> result = new HashMap<>();
        result.put("message", "Logout successful");
        return ResponseEntity.ok(result);
    }
}