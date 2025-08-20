package com.bookmark.controller;

import com.bookmark.model.Folder;
import com.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderController {
    
    private final BookmarkService bookmarkService;
    
    @GetMapping
    public ResponseEntity<List<Folder>> getAllFolders() {
        try {
            List<Folder> folders = bookmarkService.getAllFolders();
            return ResponseEntity.ok(folders);
        } catch (IOException e) {
            log.error("Failed to get folders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping
    public ResponseEntity<Folder> createFolder(@Valid @RequestBody Folder folder) {
        try {
            Folder created = bookmarkService.createFolder(folder);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IOException e) {
            log.error("Failed to create folder", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Folder> updateFolder(@PathVariable String id, @Valid @RequestBody Folder folder) {
        try {
            Folder updated = bookmarkService.updateFolder(id, folder);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            }
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Failed to update folder", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFolder(@PathVariable String id) {
        try {
            boolean deleted = bookmarkService.deleteFolder(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Failed to delete folder", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}