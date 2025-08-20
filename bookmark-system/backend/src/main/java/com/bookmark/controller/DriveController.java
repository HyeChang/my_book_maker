package com.bookmark.controller;

import com.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/drive")
@RequiredArgsConstructor
public class DriveController {
    
    private final BookmarkService bookmarkService;
    
    @GetMapping("/init")
    public ResponseEntity<Map<String, String>> initializeDrive() {
        try {
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
    
    @PostMapping("/sync")
    public ResponseEntity<Map<String, String>> syncWithDrive() {
        try {
            // TODO: Implement sync logic
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Synchronization completed");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to sync with Drive", e);
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}