package com.bookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * URL 메타데이터를 담는 DTO 클래스
 * 웹 페이지에서 스크래핑한 정보를 클라이언트에 전달하는 데 사용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class URLMetadataDTO {
    /**
     * 페이지 제목 (og:title, twitter:title, 또는 <title> 태그)
     */
    private String title;
    
    /**
     * 페이지 설명 (og:description, twitter:description, meta description, 또는 본문 요약)
     */
    private String description;
    
    /**
     * 파비콘 URL
     */
    private String favicon;
    
    /**
     * Open Graph 이미지 URL (og:image)
     */
    private String ogImage;
    
    /**
     * 사이트 이름 (og:site_name)
     */
    private String siteName;
    
    /**
     * 작성자 정보 (meta author 또는 article:author)
     */
    private String author;
    
    /**
     * 페이지 키워드 (meta keywords)
     */
    private String keywords;
}