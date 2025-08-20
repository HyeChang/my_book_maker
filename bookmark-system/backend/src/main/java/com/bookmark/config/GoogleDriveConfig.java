package com.bookmark.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Configuration
public class GoogleDriveConfig {
    
    @Value("${google.drive.application-name}")
    private String applicationName;
    
    @Value("${google.drive.tokens-directory-path}")
    private String tokensDirectoryPath;
    
    @Value("${google.drive.folder-name}")
    private String folderName;
    
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    
    @Bean
    public HttpTransport httpTransport() throws GeneralSecurityException, IOException {
        return GoogleNetHttpTransport.newTrustedTransport();
    }
    
    @Bean
    public JsonFactory jsonFactory() {
        return JSON_FACTORY;
    }
    
    public String getApplicationName() {
        return applicationName;
    }
    
    public String getTokensDirectoryPath() {
        return tokensDirectoryPath;
    }
    
    public String getFolderName() {
        return folderName;
    }
    
    public List<String> getScopes() {
        return SCOPES;
    }
}