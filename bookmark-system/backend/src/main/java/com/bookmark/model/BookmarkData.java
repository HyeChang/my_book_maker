package com.bookmark.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkData {
    @Builder.Default
    private String version = "1.0";
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime lastModified;
    
    @Builder.Default
    private List<Bookmark> bookmarks = new ArrayList<>();
    
    @Builder.Default
    private List<Folder> folders = new ArrayList<>();
    
    @Builder.Default
    private List<Tag> tags = new ArrayList<>();
}