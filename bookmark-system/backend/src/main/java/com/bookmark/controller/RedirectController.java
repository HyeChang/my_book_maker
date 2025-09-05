package com.bookmark.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

/**
 * 리다이렉션 처리를 위한 컨트롤러
 * 백엔드로 들어온 프론트엔드 경로 요청을 적절히 리다이렉트
 */
@Slf4j
@Controller  // @RestController가 아닌 @Controller 사용 (뷰 리다이렉트를 위해)
public class RedirectController {
    
    /**
     * /login 경로 요청 시 프론트엔드 로그인 페이지로 리다이렉트
     * OAuth 로그인 후 브라우저 뒤로가기 시 발생하는 404 에러 방지
     * 
     * @param response HTTP 응답 객체
     * @throws IOException 리다이렉트 실패 시
     */
    @GetMapping("/login")
    public void redirectToLogin(HttpServletResponse response) throws IOException {
        log.info("Redirecting /api/login to frontend login page");
        response.sendRedirect("http://localhost:3000/login");
    }
    
    /**
     * 루트 경로(/) 요청 시 프론트엔드 메인 페이지로 리다이렉트
     * 
     * @param response HTTP 응답 객체
     * @throws IOException 리다이렉트 실패 시
     */
    @GetMapping("/")
    public void redirectToHome(HttpServletResponse response) throws IOException {
        log.info("Redirecting to frontend home page");
        response.sendRedirect("http://localhost:3000");
    }
    
}