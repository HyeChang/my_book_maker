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

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleDriveService {
    
    private final GoogleDriveConfig driveConfig;
    private final HttpTransport httpTransport;
    private final JsonFactory jsonFactory;
    private final OAuth2AuthorizedClientService authorizedClientService;
    
    private Drive getDriveService() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName()
                );
                
                if (client != null && client.getAccessToken() != null) {
                    String accessTokenValue = client.getAccessToken().getTokenValue();
                    AccessToken accessToken = new AccessToken(accessTokenValue, new Date(System.currentTimeMillis() + 3600000));
                    GoogleCredentials credentials = GoogleCredentials.create(accessToken);
                    
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
    
    public String createBookmarkFolder() throws IOException {
        Drive service = getDriveService();
        if (service == null) {
            throw new IOException("Drive service is not available");
        }
        
        // Check if folder already exists
        String folderId = findFolderByName(driveConfig.getFolderName());
        if (folderId != null) {
            return folderId;
        }
        
        // Create new folder
        File fileMetadata = new File();
        fileMetadata.setName(driveConfig.getFolderName());
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        
        File folder = service.files().create(fileMetadata)
            .setFields("id")
            .execute();
        
        log.info("Created folder with ID: {}", folder.getId());
        return folder.getId();
    }
    
    private String findFolderByName(String folderName) throws IOException {
        Drive service = getDriveService();
        if (service == null) {
            return null;
        }
        
        String query = String.format("name='%s' and mimeType='application/vnd.google-apps.folder' and trashed=false", folderName);
        FileList result = service.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute();
        
        List<File> files = result.getFiles();
        if (files != null && !files.isEmpty()) {
            return files.get(0).getId();
        }
        return null;
    }
    
    public String readFile(String fileName, String folderId) throws IOException {
        Drive service = getDriveService();
        if (service == null) {
            throw new IOException("Drive service is not available");
        }
        
        String fileId = findFileInFolder(fileName, folderId);
        if (fileId == null) {
            return null;
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        service.files().get(fileId).executeMediaAndDownloadTo(outputStream);
        
        return outputStream.toString("UTF-8");
    }
    
    public void writeFile(String fileName, String content, String folderId) throws IOException {
        Drive service = getDriveService();
        if (service == null) {
            throw new IOException("Drive service is not available");
        }
        
        String fileId = findFileInFolder(fileName, folderId);
        ByteArrayContent mediaContent = new ByteArrayContent("application/json", content.getBytes("UTF-8"));
        
        if (fileId != null) {
            // Update existing file
            File file = new File();
            service.files().update(fileId, file, mediaContent).execute();
            log.info("Updated file: {}", fileName);
        } else {
            // Create new file
            File fileMetadata = new File();
            fileMetadata.setName(fileName);
            fileMetadata.setParents(Collections.singletonList(folderId));
            
            service.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();
            log.info("Created file: {}", fileName);
        }
    }
    
    private String findFileInFolder(String fileName, String folderId) throws IOException {
        Drive service = getDriveService();
        if (service == null) {
            return null;
        }
        
        String query = String.format("name='%s' and '%s' in parents and trashed=false", fileName, folderId);
        FileList result = service.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute();
        
        List<File> files = result.getFiles();
        if (files != null && !files.isEmpty()) {
            return files.get(0).getId();
        }
        return null;
    }
    
    public void deleteFile(String fileName, String folderId) throws IOException {
        Drive service = getDriveService();
        if (service == null) {
            throw new IOException("Drive service is not available");
        }
        
        String fileId = findFileInFolder(fileName, folderId);
        if (fileId != null) {
            service.files().delete(fileId).execute();
            log.info("Deleted file: {}", fileName);
        }
    }
    
    public List<File> listFilesInFolder(String folderId) throws IOException {
        Drive service = getDriveService();
        if (service == null) {
            throw new IOException("Drive service is not available");
        }
        
        String query = String.format("'%s' in parents and trashed=false", folderId);
        FileList result = service.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name, createdTime, modifiedTime, size)")
            .execute();
        
        return result.getFiles();
    }
}