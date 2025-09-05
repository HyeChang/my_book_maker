package com.bookmark.service;

import com.bookmark.model.Bookmark;
import com.bookmark.model.BookmarkData;
import com.bookmark.model.Folder;
import com.bookmark.model.Tag;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkService {
    
    private final GoogleDriveService driveService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
    private static final String BOOKMARKS_FILE = "bookmarks.json";
    private static final String FOLDERS_FILE = "folders.json";
    private static final String TAGS_FILE = "tags.json";
    private static final String SETTINGS_FILE = "settings.json";
    
    private String folderId;

    /**
     * Drive 구조 초기화 시도
     * @throws IOException
     */
    public void initializeDriveStructure() throws IOException {
        this.folderId = driveService.createBookmarkFolder();
        
        // Initialize data files if they don't exist
        if (driveService.readFile(BOOKMARKS_FILE, folderId) == null) {
            BookmarkData initialData = BookmarkData.builder()
                .version("1.0")
                .lastModified(LocalDateTime.now())
                .bookmarks(new ArrayList<>())
                .folders(createDefaultFolders())
                .tags(new ArrayList<>())
                .build();
            
            saveBookmarkData(initialData);
            log.info("Initialized bookmark data structure in Google Drive");
        }
    }
    
    private List<Folder> createDefaultFolders() {
        List<Folder> folders = new ArrayList<>();
        folders.add(Folder.builder()
            .id(UUID.randomUUID().toString())
            .name("일반")
            .parentId(null)
            .isLocked(false)
            .color("#4285F4")
            .icon("folder")
            .order(1)
            .build());
        
        folders.add(Folder.builder()
            .id(UUID.randomUUID().toString())
            .name("중요")
            .parentId(null)
            .isLocked(false)
            .color("#EA4335")
            .icon("star")
            .order(2)
            .build());
        
        return folders;
    }
    
    public BookmarkData loadBookmarkData() throws IOException {
        String content = driveService.readFile(BOOKMARKS_FILE, folderId);
        if (content == null) {
            return BookmarkData.builder().build();
        }
        return objectMapper.readValue(content, BookmarkData.class);
    }
    
    private void saveBookmarkData(BookmarkData data) throws IOException {
        data.setLastModified(LocalDateTime.now());
        String content = objectMapper.writeValueAsString(data);
        driveService.writeFile(BOOKMARKS_FILE, content, folderId);
    }
    
    public List<Bookmark> getAllBookmarks() throws IOException {
        BookmarkData data = loadBookmarkData();
        return data.getBookmarks();
    }
    
    public Bookmark getBookmarkById(String id) throws IOException {
        BookmarkData data = loadBookmarkData();
        return data.getBookmarks().stream()
            .filter(b -> b.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    public Bookmark createBookmark(Bookmark bookmark) throws IOException {
        BookmarkData data = loadBookmarkData();
        
        bookmark.setId(UUID.randomUUID().toString());
        bookmark.setCreatedAt(LocalDateTime.now());
        bookmark.setUpdatedAt(LocalDateTime.now());
        
        if (bookmark.getMetadata() == null) {
            bookmark.setMetadata(Bookmark.BookmarkMetadata.builder()
                .visitCount(0)
                .build());
        }
        
        data.getBookmarks().add(bookmark);
        saveBookmarkData(data);
        
        log.info("Created bookmark: {}", bookmark.getId());
        return bookmark;
    }
    
    public Bookmark updateBookmark(String id, Bookmark updatedBookmark) throws IOException {
        BookmarkData data = loadBookmarkData();
        
        for (int i = 0; i < data.getBookmarks().size(); i++) {
            Bookmark bookmark = data.getBookmarks().get(i);
            if (bookmark.getId().equals(id)) {
                updatedBookmark.setId(id);
                updatedBookmark.setCreatedAt(bookmark.getCreatedAt());
                updatedBookmark.setUpdatedAt(LocalDateTime.now());
                data.getBookmarks().set(i, updatedBookmark);
                saveBookmarkData(data);
                log.info("Updated bookmark: {}", id);
                return updatedBookmark;
            }
        }
        
        return null;
    }
    
    public boolean deleteBookmark(String id) throws IOException {
        BookmarkData data = loadBookmarkData();
        
        boolean removed = data.getBookmarks().removeIf(b -> b.getId().equals(id));
        if (removed) {
            saveBookmarkData(data);
            log.info("Deleted bookmark: {}", id);
        }
        
        return removed;
    }
    
    public List<Bookmark> searchBookmarks(String query) throws IOException {
        BookmarkData data = loadBookmarkData();
        String lowerQuery = query.toLowerCase();
        
        return data.getBookmarks().stream()
            .filter(b -> 
                (b.getTitle() != null && b.getTitle().toLowerCase().contains(lowerQuery)) ||
                (b.getDescription() != null && b.getDescription().toLowerCase().contains(lowerQuery)) ||
                (b.getUrl() != null && b.getUrl().toLowerCase().contains(lowerQuery)) ||
                (b.getTags() != null && b.getTags().stream().anyMatch(t -> t.toLowerCase().contains(lowerQuery)))
            )
            .collect(Collectors.toList());
    }
    
    public List<Bookmark> getBookmarksByFolder(String folderId) throws IOException {
        BookmarkData data = loadBookmarkData();
        return data.getBookmarks().stream()
            .filter(b -> folderId.equals(b.getFolderId()))
            .collect(Collectors.toList());
    }
    
    public List<Bookmark> getBookmarksByTag(String tag) throws IOException {
        BookmarkData data = loadBookmarkData();
        return data.getBookmarks().stream()
            .filter(b -> b.getTags() != null && b.getTags().contains(tag))
            .collect(Collectors.toList());
    }
    
    // Folder management
    public List<Folder> getAllFolders() throws IOException {
        BookmarkData data = loadBookmarkData();
        return data.getFolders();
    }
    
    public Folder createFolder(Folder folder) throws IOException {
        BookmarkData data = loadBookmarkData();
        
        folder.setId(UUID.randomUUID().toString());
        if (folder.getOrder() == null) {
            folder.setOrder(data.getFolders().size() + 1);
        }
        
        data.getFolders().add(folder);
        saveBookmarkData(data);
        
        log.info("Created folder: {}", folder.getId());
        return folder;
    }
    
    public Folder updateFolder(String id, Folder updatedFolder) throws IOException {
        BookmarkData data = loadBookmarkData();
        
        for (int i = 0; i < data.getFolders().size(); i++) {
            Folder folder = data.getFolders().get(i);
            if (folder.getId().equals(id)) {
                updatedFolder.setId(id);
                data.getFolders().set(i, updatedFolder);
                saveBookmarkData(data);
                log.info("Updated folder: {}", id);
                return updatedFolder;
            }
        }
        
        return null;
    }
    
    public boolean deleteFolder(String id) throws IOException {
        BookmarkData data = loadBookmarkData();
        
        // Move bookmarks from deleted folder to default folder
        String defaultFolderId = data.getFolders().get(0).getId();
        data.getBookmarks().stream()
            .filter(b -> id.equals(b.getFolderId()))
            .forEach(b -> b.setFolderId(defaultFolderId));
        
        boolean removed = data.getFolders().removeIf(f -> f.getId().equals(id));
        if (removed) {
            saveBookmarkData(data);
            log.info("Deleted folder: {}", id);
        }
        
        return removed;
    }
    
    // Tag management
    public List<Tag> getAllTags() throws IOException {
        BookmarkData data = loadBookmarkData();
        return data.getTags();
    }
    
    public Tag createTag(Tag tag) throws IOException {
        BookmarkData data = loadBookmarkData();
        
        tag.setId(UUID.randomUUID().toString());
        tag.setUsageCount(0);
        
        data.getTags().add(tag);
        saveBookmarkData(data);
        
        log.info("Created tag: {}", tag.getId());
        return tag;
    }
    
    public boolean deleteTag(String id) throws IOException {
        BookmarkData data = loadBookmarkData();
        
        // Remove tag from all bookmarks
        data.getBookmarks().forEach(b -> {
            if (b.getTags() != null) {
                b.getTags().removeIf(t -> t.equals(id));
            }
        });
        
        boolean removed = data.getTags().removeIf(t -> t.getId().equals(id));
        if (removed) {
            saveBookmarkData(data);
            log.info("Deleted tag: {}", id);
        }
        
        return removed;
    }
}