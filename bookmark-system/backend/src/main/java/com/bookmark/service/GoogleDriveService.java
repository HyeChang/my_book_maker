package com.bookmark.service;

import com.bookmark.config.GoogleDriveConfig;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Google Drive API를 통해 사용자의 Drive에 파일을 읽고 쓰는 서비스
 * 
 * 이 서비스는 OAuth2로 인증된 사용자의 Google Drive에 접근하여
 * 북마크 데이터를 JSON 파일 형태로 저장하고 관리합니다.
 * 
 * @author Bookmark System
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleDriveService {
    
    /** Google Drive 관련 설정 (애플리케이션 이름, 폴더 이름 등) */
    private final GoogleDriveConfig driveConfig;
    
    /** HTTP 통신을 위한 전송 객체 */
    private final HttpTransport httpTransport;
    
    /** JSON 파싱 및 생성을 위한 팩토리 */
    private final JsonFactory jsonFactory;
    
    /** OAuth2 인증된 클라이언트 정보를 관리하는 서비스 */
    private final OAuth2AuthorizedClientService authorizedClientService;
    
    /**
     * 현재 인증된 사용자의 Google Drive 서비스 인스턴스를 생성합니다.
     * 
     * Spring Security 컨텍스트에서 OAuth2 토큰을 가져와서
     * Google Drive API 클라이언트를 초기화합니다.
     * 
     * @return 인증된 Drive 서비스 인스턴스, 인증 실패시 null
     */
    private Drive getDriveService() {
        try {
            // Spring Security 컨텍스트에서 현재 인증 정보를 가져옴
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // OAuth2로 인증된 경우에만 처리
            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                
                // 인증된 클라이언트 정보 로드 (Google OAuth2 토큰 포함)
                OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName()
                );
                
                // 유효한 액세스 토큰이 있는 경우 Drive 서비스 생성
                if (client != null && client.getAccessToken() != null) {
                    String accessTokenValue = client.getAccessToken().getTokenValue();
                    
                    // Google 인증 객체 생성 (토큰 만료 시간을 1시간으로 설정)
                    AccessToken accessToken = new AccessToken(accessTokenValue, new Date(System.currentTimeMillis() + 3600000));
                    GoogleCredentials credentials = GoogleCredentials.create(accessToken);
                    
                    // Google Drive API 클라이언트 빌드
                    return new Drive.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(credentials))
                        .setApplicationName(driveConfig.getApplicationName())
                        .build();
                }
            }
        } catch (Exception e) {
            log.error("Failed to create Drive service", e);
        }
        return null;
    }
    
    /**
     * 북마크 데이터를 저장할 Google Drive 폴더를 생성합니다.
     * 
     * 이미 동일한 이름의 폴더가 존재하면 해당 폴더의 ID를 반환하고,
     * 없으면 새로 생성합니다.
     * 
     * @return 생성되거나 찾은 폴더의 Google Drive ID
     * @throws IOException Drive 서비스를 사용할 수 없거나 API 호출 실패시
     */
    public String createBookmarkFolder() throws IOException {
        // Drive 서비스 인스턴스 가져오기
        Drive service = getDriveService();
        if (service == null) {
            throw new IOException("Drive service is not available");
        }
        
        // 동일한 이름의 폴더가 이미 존재하는지 확인
        String folderId = findFolderByName(driveConfig.getFolderName());
        if (folderId != null) {
            // 이미 존재하면 해당 폴더 ID 반환
            return folderId;
        }
        
        // 새 폴더 생성을 위한 메타데이터 설정
        File fileMetadata = new File();
        fileMetadata.setName(driveConfig.getFolderName());
        fileMetadata.setMimeType("application/vnd.google-apps.folder"); // Google Drive 폴더 MIME 타입
        
        // Drive API를 통해 폴더 생성
        File folder = service.files().create(fileMetadata)
            .setFields("id") // 응답에서 ID 필드만 가져오기 (성능 최적화)
            .execute();
        
        log.info("Created folder with ID: {}", folder.getId());
        return folder.getId();
    }
    
    /**
     * 이름으로 Google Drive 폴더를 검색합니다.
     * 
     * @param folderName 검색할 폴더 이름
     * @return 찾은 폴더의 ID, 없으면 null
     * @throws IOException Drive API 호출 실패시
     */
    private String findFolderByName(String folderName) throws IOException {
        Drive service = getDriveService();
        if (service == null) {
            return null;
        }
        
        // Drive API 쿼리 문법으로 검색 조건 생성
        // - name: 폴더 이름
        // - mimeType: 폴더 타입으로 필터링
        // - trashed: 휴지통에 있는 파일 제외
        String query = String.format("name='%s' and mimeType='application/vnd.google-apps.folder' and trashed=false", folderName);
        
        // Drive에서 조건에 맞는 파일 검색
        FileList result = service.files().list()
            .setQ(query)                     // 검색 쿼리 설정
            .setSpaces("drive")              // 검색 공간 (drive, appDataFolder, photos)
            .setFields("files(id, name)")    // 필요한 필드만 가져오기 (성능 최적화)
            .execute();
        
        // 검색 결과에서 첫 번째 폴더의 ID 반환
        List<File> files = result.getFiles();
        if (files != null && !files.isEmpty()) {
            return files.get(0).getId();
        }
        return null;
    }
    
    /**
     * Google Drive 폴더에서 파일을 읽어 내용을 반환합니다.
     * 
     * @param fileName 읽을 파일 이름
     * @param folderId 파일이 있는 폴더의 ID
     * @return 파일 내용 (UTF-8 문자열), 파일이 없으면 null
     * @throws IOException Drive API 호출 실패시
     */
    public String readFile(String fileName, String folderId) throws IOException {
        Drive service = getDriveService();
        if (service == null) {
            throw new IOException("Drive service is not available");
        }
        
        // 폴더 내에서 파일 검색
        String fileId = findFileInFolder(fileName, folderId);
        if (fileId == null) {
            // 파일이 존재하지 않음
            return null;
        }
        
        // 파일 내용을 메모리로 다운로드
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        service.files().get(fileId)
            .executeMediaAndDownloadTo(outputStream); // 파일의 실제 내용을 다운로드
        
        // UTF-8 인코딩으로 문자열 변환
        return outputStream.toString("UTF-8");
    }
    
    /**
     * Google Drive 폴더에 파일을 작성하거나 업데이트합니다.
     * 
     * 파일이 이미 존재하면 내용을 업데이트하고,
     * 없으면 새로 생성합니다.
     * 
     * @param fileName 작성할 파일 이름
     * @param content 파일에 저장할 내용 (JSON 문자열)
     * @param folderId 파일을 저장할 폴더의 ID
     * @throws IOException Drive API 호출 실패시
     */
    public void writeFile(String fileName, String content, String folderId) throws IOException {
        Drive service = getDriveService();
        if (service == null) {
            throw new IOException("Drive service is not available");
        }
        
        // 동일한 이름의 파일이 이미 존재하는지 확인
        String fileId = findFileInFolder(fileName, folderId);
        
        // 파일 내용을 바이트 배열로 변환 (JSON 타입으로 설정)
        ByteArrayContent mediaContent = new ByteArrayContent("application/json", content.getBytes("UTF-8"));
        
        if (fileId != null) {
            // 기존 파일이 있으면 내용 업데이트
            File file = new File();
            service.files().update(fileId, file, mediaContent).execute();
            log.info("Updated file: {}", fileName);
        } else {
            // 새 파일 생성
            File fileMetadata = new File();
            fileMetadata.setName(fileName);
            fileMetadata.setParents(Collections.singletonList(folderId)); // 부모 폴더 설정
            
            // Drive API를 통해 파일 생성
            service.files().create(fileMetadata, mediaContent)
                .setFields("id") // 응답에서 ID만 가져오기
                .execute();
            log.info("Created file: {}", fileName);
        }
    }
    
    /**
     * 특정 폴더 내에서 파일을 검색합니다.
     * 
     * @param fileName 검색할 파일 이름
     * @param folderId 검색할 폴더의 ID
     * @return 찾은 파일의 ID, 없으면 null
     * @throws IOException Drive API 호출 실패시
     */
    private String findFileInFolder(String fileName, String folderId) throws IOException {
        Drive service = getDriveService();
        if (service == null) {
            return null;
        }
        
        // Drive API 검색 쿼리 생성
        // - name: 파일 이름
        // - parents: 부모 폴더 ID (특정 폴더 내에서만 검색)
        // - trashed: 휴지통에 있는 파일 제외
        String query = String.format("name='%s' and '%s' in parents and trashed=false", fileName, folderId);
        
        // Drive에서 파일 검색
        FileList result = service.files().list()
            .setQ(query)                   // 검색 쿼리
            .setSpaces("drive")            // 검색 공간
            .setFields("files(id, name)")  // 필요한 필드만 가져오기
            .execute();
        
        // 검색 결과에서 첫 번째 파일의 ID 반환
        List<File> files = result.getFiles();
        if (files != null && !files.isEmpty()) {
            return files.get(0).getId();
        }
        return null;
    }
    
    /**
     * Google Drive 폴더에서 파일을 삭제합니다.
     * 
     * @param fileName 삭제할 파일 이름
     * @param folderId 파일이 있는 폴더의 ID
     * @throws IOException Drive API 호출 실패시
     */
    public void deleteFile(String fileName, String folderId) throws IOException {
        Drive service = getDriveService();
        if (service == null) {
            throw new IOException("Drive service is not available");
        }
        
        // 삭제할 파일 검색
        String fileId = findFileInFolder(fileName, folderId);
        if (fileId != null) {
            // Drive API를 통해 파일 삭제
            service.files().delete(fileId).execute();
            log.info("Deleted file: {}", fileName);
        }
        // 파일이 없는 경우는 무시 (이미 삭제된 것으로 간주)
    }
    
    /**
     * 특정 폴더 내의 모든 파일 목록을 조회합니다.
     * 
     * @param folderId 조회할 폴더의 ID
     * @return 폴더 내 파일 목록 (각 파일의 ID, 이름, 생성시간, 수정시간, 크기 포함)
     * @throws IOException Drive API 호출 실패시
     */
    public List<File> listFilesInFolder(String folderId) throws IOException {
        Drive service = getDriveService();
        if (service == null) {
            throw new IOException("Drive service is not available");
        }
        
        // 특정 폴더 내의 파일들만 검색하는 쿼리
        String query = String.format("'%s' in parents and trashed=false", folderId);
        
        // Drive API를 통해 파일 목록 조회
        FileList result = service.files().list()
            .setQ(query)                                              // 검색 쿼리
            .setSpaces("drive")                                       // 검색 공간
            .setFields("files(id, name, createdTime, modifiedTime, size)") // 조회할 파일 정보 필드
            .execute();
        
        return result.getFiles();
    }
}