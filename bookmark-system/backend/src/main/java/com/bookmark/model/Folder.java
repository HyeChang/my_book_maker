package com.bookmark.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Folder {
    private String id;
    private String name;
    private String parentId;
    private Boolean isLocked;
    private String passwordHash;
    private String color;
    private String icon;
    private Integer order;
}