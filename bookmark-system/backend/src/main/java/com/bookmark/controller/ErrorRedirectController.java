package com.bookmark.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * 404 에러 및 기타 에러 발생 시 프론트엔드로 리다이렉트하는 컨트롤러
 * Spring Boot의 기본 에러 처리를 오버라이드
 */
@Slf4j
@Controller
public class ErrorRedirectController implements ErrorController {
    
    /**
     * 모든 에러를 처리하는 엔드포인트
     * 404 에러의 경우 프론트엔드로 리다이렉트
     * 
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @throws IOException 리다이렉트 실패 시
     */
    @RequestMapping("/error")
    public void handleError(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            // 404 에러의 경우 요청 URL에 따라 적절한 프론트엔드 페이지로 리다이렉트
            if (statusCode == 404) {
                String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
                log.info("404 error for URI: {}, redirecting to frontend", requestUri);
                
                // OAuth2 관련 URL이거나 login이 포함된 경우 로그인 페이지로
                if (requestUri != null && (requestUri.contains("oauth2") || requestUri.contains("login"))) {
                    response.sendRedirect("http://localhost:3000/login");
                } else {
                    // 그 외의 경우 메인 페이지로
                    response.sendRedirect("http://localhost:3000");
                }
                return;
            }
        }
        
        // 기타 에러의 경우 에러 정보를 포함하여 프론트엔드로 리다이렉트
        log.error("Error occurred: status={}", status);
        response.sendRedirect("http://localhost:3000/error");
    }
}