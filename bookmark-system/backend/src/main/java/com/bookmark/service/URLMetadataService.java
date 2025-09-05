package com.bookmark.service;

import com.bookmark.dto.URLMetadataDTO;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * URL 메타데이터 추출 서비스
 * Jsoup을 사용하여 웹 페이지의 메타 정보를 스크래핑하고
 * 제목, 설명, 파비콘 등의 정보를 추출
 */
@Slf4j
@Service
public class URLMetadataService {
    
    // 타임아웃 설정 (초 단위)
    private static final int TIMEOUT_SECONDS = 5;
    // 브라우저 User-Agent 설정 (일부 사이트는 봇을 차단하므로)
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    
    /**
     * URL에서 메타데이터를 비동기적으로 추출
     * @param urlString 메타데이터를 추출할 URL
     * @return 추출된 메타데이터 DTO
     */
    public URLMetadataDTO fetchMetadata(String urlString) {
        try {
            return CompletableFuture
                .supplyAsync(() -> scrapeMetadata(urlString))
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Failed to fetch metadata for URL: {}", urlString, e);
            return URLMetadataDTO.builder()
                .title(extractDomainName(urlString))
                .description("Failed to fetch page information")
                .build();
        }
    }
    
    /**
     * 실제 웹 스크래핑을 수행하는 메소드
     * @param urlString 스크래핑할 URL
     * @return 스크래핑된 메타데이터
     */
    private URLMetadataDTO scrapeMetadata(String urlString) {
        try {
            Document doc = Jsoup.connect(urlString)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_SECONDS * 1000)
                .followRedirects(true)
                .get();
            
            URLMetadataDTO metadata = URLMetadataDTO.builder()
                .title(extractTitle(doc))
                .description(extractDescription(doc))
                .favicon(extractFavicon(doc, urlString))
                .ogImage(extractMetaContent(doc, "og:image"))
                .siteName(extractMetaContent(doc, "og:site_name"))
                .author(extractAuthor(doc))
                .keywords(extractMetaContent(doc, "keywords"))
                .build();
            
            // If no description found, try to generate summary from content
            if (metadata.getDescription() == null || metadata.getDescription().isEmpty()) {
                metadata.setDescription(generateSummary(doc));
            }
            
            return metadata;
            
        } catch (IOException e) {
            log.error("Error scraping metadata from URL: {}", urlString, e);
            throw new RuntimeException("Failed to scrape metadata", e);
        }
    }
    
    /**
     * 페이지 제목 추출
     * 우선순위: OpenGraph > Twitter Card > HTML title 태그
     * @param doc Jsoup Document 객체
     * @return 추출된 제목
     */
    private String extractTitle(Document doc) {
        // Try OpenGraph title first
        String ogTitle = extractMetaContent(doc, "og:title");
        if (ogTitle != null && !ogTitle.isEmpty()) {
            return ogTitle;
        }
        
        // Try Twitter title
        String twitterTitle = extractMetaContent(doc, "twitter:title");
        if (twitterTitle != null && !twitterTitle.isEmpty()) {
            return twitterTitle;
        }
        
        // Fallback to page title
        return doc.title();
    }
    
    /**
     * 페이지 설명 추출
     * 우선순위: OpenGraph > Twitter Card > meta description
     * @param doc Jsoup Document 객체
     * @return 추출된 설명
     */
    private String extractDescription(Document doc) {
        // Try OpenGraph description first
        String ogDesc = extractMetaContent(doc, "og:description");
        if (ogDesc != null && !ogDesc.isEmpty()) {
            return ogDesc;
        }
        
        // Try Twitter description
        String twitterDesc = extractMetaContent(doc, "twitter:description");
        if (twitterDesc != null && !twitterDesc.isEmpty()) {
            return twitterDesc;
        }
        
        // Try standard meta description
        String metaDesc = extractMetaContent(doc, "description");
        if (metaDesc != null && !metaDesc.isEmpty()) {
            return metaDesc;
        }
        
        return null;
    }
    
    /**
     * 파비콘 URL 추출
     * 다양한 형식의 파비콘 태그를 확인하고 절대 URL로 변환
     * @param doc Jsoup Document 객체
     * @param urlString 원본 URL (기본 파비콘 경로 생성용)
     * @return 파비콘 URL
     */
    private String extractFavicon(Document doc, String urlString) {
        try {
            // Try to find various favicon formats
            String[] faviconSelectors = {
                "link[rel='icon']",
                "link[rel='shortcut icon']",
                "link[rel='apple-touch-icon']",
                "link[rel='apple-touch-icon-precomposed']"
            };
            
            for (String selector : faviconSelectors) {
                Element favicon = doc.selectFirst(selector);
                if (favicon != null) {
                    String href = favicon.attr("abs:href");
                    if (!href.isEmpty()) {
                        return href;
                    }
                }
            }
            
            // Default favicon path
            URI uri = new URI(urlString);
            return uri.getScheme() + "://" + uri.getHost() + "/favicon.ico";
            
        } catch (URISyntaxException e) {
            log.error("Invalid URL for favicon extraction: {}", urlString);
            return null;
        }
    }
    
    /**
     * 메타 태그의 content 속성 값 추출
     * property 또는 name 속성으로 메타 태그를 찾음
     * @param doc Jsoup Document 객체
     * @param property 찾을 메타 태그의 property/name 값
     * @return content 속성 값
     */
    private String extractMetaContent(Document doc, String property) {
        // Try property attribute
        Element meta = doc.selectFirst("meta[property='" + property + "']");
        if (meta != null) {
            return meta.attr("content");
        }
        
        // Try name attribute
        meta = doc.selectFirst("meta[name='" + property + "']");
        if (meta != null) {
            return meta.attr("content");
        }
        
        return null;
    }
    
    /**
     * 작성자 정보 추출
     * @param doc Jsoup Document 객체
     * @return 작성자 이름
     */
    private String extractAuthor(Document doc) {
        String author = extractMetaContent(doc, "author");
        if (author != null && !author.isEmpty()) {
            return author;
        }
        
        // Try article:author
        author = extractMetaContent(doc, "article:author");
        if (author != null && !author.isEmpty()) {
            return author;
        }
        
        return null;
    }
    
    /**
     * 페이지 본문에서 요약 생성
     * description이 없을 경우 첫 번째 단락이나 의미있는 텍스트를 추출
     * @param doc Jsoup Document 객체
     * @return 생성된 요약 (최대 200자)
     */
    private String generateSummary(Document doc) {
        // Try to extract meaningful content from the page
        String[] contentSelectors = {
            "main p:first-of-type",
            "article p:first-of-type",
            "[role='main'] p:first-of-type",
            ".content p:first-of-type",
            "#content p:first-of-type",
            "p:first-of-type"
        };
        
        for (String selector : contentSelectors) {
            Element paragraph = doc.selectFirst(selector);
            if (paragraph != null) {
                String text = paragraph.text().trim();
                if (!text.isEmpty() && text.length() > 20) {
                    // Limit to 200 characters
                    if (text.length() > 200) {
                        text = text.substring(0, 197) + "...";
                    }
                    return text;
                }
            }
        }
        
        // If no good paragraph found, try to get any text content
        String bodyText = doc.body().text();
        if (bodyText != null && bodyText.length() > 50) {
            // Remove common navigation text patterns
            bodyText = bodyText.replaceAll("(Home|About|Contact|Menu|Navigation|Cookie|Privacy|Terms)\\s*", "");
            
            // Get first meaningful sentence
            String[] sentences = bodyText.split("[.!?]");
            for (String sentence : sentences) {
                String trimmed = sentence.trim();
                if (trimmed.length() > 20) {
                    if (trimmed.length() > 200) {
                        trimmed = trimmed.substring(0, 197) + "...";
                    }
                    return trimmed;
                }
            }
        }
        
        return "";
    }
    
    /**
     * URL에서 도메인 이름 추출
     * @param urlString URL 문자열
     * @return 도메인 이름 (예: www.example.com)
     */
    private String extractDomainName(String urlString) {
        try {
            URI uri = new URI(urlString);
            return uri.getHost();
        } catch (URISyntaxException e) {
            return urlString;
        }
    }
}