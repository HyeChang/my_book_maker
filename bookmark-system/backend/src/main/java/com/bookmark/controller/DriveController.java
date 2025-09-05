package com.bookmark.controller;

import com.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Google Drive 관련 작업을 처리하는 컨트롤러
 * 
 * 이 컨트롤러는 사용자의 Google Drive에 북마크 데이터를 저장하고 관리하는 기능을 제공합니다.
 * 모든 엔드포인트는 인증된 사용자만 접근 가능하며, 각 사용자의 Drive에 독립적인 데이터를 관리합니다.
 */

@Slf4j
@RestController
@RequestMapping("/drive")
@RequiredArgsConstructor
public class DriveController {
    
    private final BookmarkService bookmarkService;

    /**
     * Google Drive에 북마크 저장용 폴더 구조를 초기화하는 엔드포인트
     * 
     * API 호출 시 동작:
     * 1. 사용자의 Google Drive에 'BookmarkService' 폴더 생성
     * 2. 필요한 JSON 파일들 초기화 (bookmarks.json, folders.json, tags.json, settings.json)
     * 3. 성공/실패 여부를 JSON 형태로 반환
     * 
     * 사용 시나리오:
     * - 프론트엔드에서 직접 API 호출이 필요한 경우
     * - 수동으로 Drive 구조를 초기화하고 싶을 때
     * 
     * @return ResponseEntity<Map<String, String>> JSON 형태의 응답
     */
    @GetMapping("/init")
    public ResponseEntity<Map<String, String>> initializeDrive() {
        try {
            // BookmarkService를 통해 Drive 구조 초기화
            bookmarkService.initializeDriveStructure();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Google Drive structure initialized successfully");
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Failed to initialize Drive structure", e);
            
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Google OAuth2 로그인 성공 후 호출되는 엔드포인트
     * Drive 구조를 초기화하고 프론트엔드로 리다이렉트
     * 
     * 동작 흐름:
     * 1. 사용자가 Google 로그인 성공
     * 2. Spring Security가 이 엔드포인트로 리다이렉트
     * 3. Google Drive 구조 초기화 시도
     * 4. 성공 시 프론트엔드 메인 페이지로 리다이렉트
     * 5. 실패 시 에러 파라미터와 함께 로그인 페이지로 리다이렉트
     * 
     * @param response HttpServletResponse 객체 (리다이렉트를 위해 사용)
     */
    @GetMapping("/init-and-redirect")
    public void initializeAndRedirect(HttpServletResponse response) {
        try {
            // Drive 구조 초기화 시도
            bookmarkService.initializeDriveStructure();
            
            log.info("Google Drive structure initialized successfully, redirecting to frontend");
            
            // JavaScript를 사용하여 브라우저 히스토리를 대체하면서 리다이렉트
            // 이렇게 하면 뒤로가기 시 백엔드 URL이 히스토리에 남지 않음
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(
                "<!DOCTYPE html>" +
                "<html><head><title>Redirecting...</title></head>" +
                "<body><script>" +
                "window.location.replace('http://localhost:3000');" +
                "</script>" +
                "<noscript>Please <a href='http://localhost:3000'>click here</a> to continue.</noscript>" +
                "</body></html>"
            );
            response.getWriter().flush();
        } catch (IOException e) {
            log.error("Failed to initialize Drive structure or redirect", e);
            
            try {
                // 에러 발생 시에도 JavaScript 리다이렉트 사용
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write(
                    "<!DOCTYPE html>" +
                    "<html><head><title>Redirecting...</title></head>" +
                    "<body><script>" +
                    "window.location.replace('http://localhost:3000/login?error=drive_init_failed');" +
                    "</script>" +
                    "<noscript>Please <a href='http://localhost:3000/login?error=drive_init_failed'>click here</a> to continue.</noscript>" +
                    "</body></html>"
                );
                response.getWriter().flush();
            } catch (IOException redirectError) {
                // 리다이렉트 자체 실패
                log.error("Failed to redirect on error", redirectError);
            }
        }
    }
    
    /**
     * Google Drive와 로컬 데이터를 동기화하는 엔드포인트
     * 
     * 향후 구현 예정 기능:
     * - Drive에서 최신 데이터 가져오기
     * - 로컬 변경사항을 Drive에 업로드
     * - 충돌 해결 로직
     * - 양방향 동기화
     * 
     * @return ResponseEntity<Map<String, String>> 동기화 결과
     *         - 현재는 구현되지 않은 상태로 더미 응답 반환
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, String>> syncWithDrive() {
        try {
            // TODO: 실제 동기화 로직 구현 필요
            // 1. Drive에서 현재 데이터 읽기
            // 2. 로컬 데이터와 비교
            // 3. 변경사항 병합
            // 4. Drive에 업데이트
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Synchronization completed");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 동기화 실패 로그
            log.error("Failed to sync with Drive", e);
            
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}