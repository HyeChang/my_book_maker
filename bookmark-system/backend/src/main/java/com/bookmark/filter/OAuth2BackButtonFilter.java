package com.bookmark.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * OAuth2 인증 경로에 대한 뒤로가기 처리 필터
 * 이미 인증된 사용자가 OAuth2 인증 URL에 접근하려고 할 때 프론트엔드로 리다이렉트
 */
@Slf4j
@Component
public class OAuth2BackButtonFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        // OAuth2 인증 경로인지 확인
        if (requestURI.contains("/oauth2/authorization/google")) {
            // 브라우저 캐싱 방지 헤더 추가
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // 이미 인증된 사용자인 경우
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getPrincipal().equals("anonymousUser")) {
                log.info("Authenticated user attempting to access OAuth2 URL, redirecting to home");
                response.sendRedirect("http://localhost:3000");
                return;
            }
            
            // 세션에서 OAuth2 진행 상태 확인
            Boolean oauth2InProgress = (Boolean) request.getSession().getAttribute("oauth2InProgress");
            if (oauth2InProgress != null && oauth2InProgress) {
                log.info("OAuth2 already in progress, redirecting to login");
                request.getSession().removeAttribute("oauth2InProgress");
                response.sendRedirect("http://localhost:3000/login");
                return;
            }
            
            // 세션에 OAuth2 진행 중 마커 설정
            request.getSession().setAttribute("oauth2InProgress", true);
        }
        
        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }
}