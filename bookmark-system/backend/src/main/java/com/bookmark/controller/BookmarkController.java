package com.bookmark.controller;

import com.bookmark.dto.URLMetadataDTO;
import com.bookmark.model.Bookmark;
import com.bookmark.service.BookmarkService;
import com.bookmark.service.URLMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {
    
    private final BookmarkService bookmarkService;
    private final URLMetadataService urlMetadataService;
    
    @GetMapping
    public ResponseEntity<List<Bookmark>> getAllBookmarks() {
        try {
            List<Bookmark> bookmarks = bookmarkService.getAllBookmarks();
            return ResponseEntity.ok(bookmarks);
        } catch (IOException e) {
            log.error("Failed to get bookmarks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Bookmark> getBookmarkById(@PathVariable String id) {
        try {
            Bookmark bookmark = bookmarkService.getBookmarkById(id);
            if (bookmark != null) {
                return ResponseEntity.ok(bookmark);
            }
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Failed to get bookmark", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping
    public ResponseEntity<Bookmark> createBookmark(@Valid @RequestBody Bookmark bookmark) {
        try {
            Bookmark created = bookmarkService.createBookmark(bookmark);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IOException e) {
            log.error("Failed to create bookmark", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Bookmark> updateBookmark(@PathVariable String id, @Valid @RequestBody Bookmark bookmark) {
        try {
            Bookmark updated = bookmarkService.updateBookmark(id, bookmark);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            }
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Failed to update bookmark", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookmark(@PathVariable String id) {
        try {
            boolean deleted = bookmarkService.deleteBookmark(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Failed to delete bookmark", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Bookmark>> searchBookmarks(@RequestParam String q) {
        try {
            List<Bookmark> bookmarks = bookmarkService.searchBookmarks(q);
            return ResponseEntity.ok(bookmarks);
        } catch (IOException e) {
            log.error("Failed to search bookmarks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/folder/{folderId}")
    public ResponseEntity<List<Bookmark>> getBookmarksByFolder(@PathVariable String folderId) {
        try {
            List<Bookmark> bookmarks = bookmarkService.getBookmarksByFolder(folderId);
            return ResponseEntity.ok(bookmarks);
        } catch (IOException e) {
            log.error("Failed to get bookmarks by folder", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<Bookmark>> getBookmarksByTag(@PathVariable String tag) {
        try {
            List<Bookmark> bookmarks = bookmarkService.getBookmarksByTag(tag);
            return ResponseEntity.ok(bookmarks);
        } catch (IOException e) {
            log.error("Failed to get bookmarks by tag", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/fetch-metadata")
    public ResponseEntity<URLMetadataDTO> fetchUrlMetadata(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        
        if (url == null || url.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            URLMetadataDTO metadata = urlMetadataService.fetchMetadata(url);
            return ResponseEntity.ok(metadata);
        } catch (Exception e) {
            log.error("Failed to fetch URL metadata for: {}", url, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}