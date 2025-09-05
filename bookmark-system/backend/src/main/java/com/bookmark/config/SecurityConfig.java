package com.bookmark.config;

import com.bookmark.filter.OAuth2BackButtonFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private OAuth2BackButtonFilter oAuth2BackButtonFilter;
    
    @Value("#{'${cors.allowed-origins:http://localhost:3000}'.split(',')}")
    private List<String> allowedOrigins;
    
    @Value("#{'${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}'.split(',')}")
    private List<String> allowedMethods;
    
    @Value("#{'${cors.allowed-headers:*}'.split(',')}")
    private List<String> allowedHeaders;
    
    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;
    
    @Value("${cors.max-age:3600}")
    private long maxAge;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // OAuth2 뒤로가기 필터를 가장 먼저 추가
            .addFilterBefore(oAuth2BackButtonFilter, UsernamePasswordAuthenticationFilter.class)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // 인증 없이 접근 가능한 경로들
                .requestMatchers("/", "/error", "/login", "/login**", "/oauth2/**").permitAll()
                .requestMatchers("/drive/init-and-redirect").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler((request, response, authentication) -> {
                    // Drive 초기화
                    response.sendRedirect("/api/drive/init-and-redirect");
                })
                .failureUrl("/login?error=true")
            )
            // 로그아웃 설정
            .logout(logout -> logout
                .logoutUrl("/auth/logout")  // 로그아웃 엔드포인트 설정
                .logoutSuccessHandler((request, response, authentication) -> {
                    // 로그아웃 성공 시 200 OK 응답 반환
                    response.setStatus(200);
                    response.getWriter().flush();
                })
                .invalidateHttpSession(true)  // 세션 무효화
                .deleteCookies("JSESSIONID")  // 세션 쿠키 삭제
                .clearAuthentication(true)    // 인증 정보 클리어
            );
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(allowedMethods);
        configuration.setAllowedHeaders(allowedHeaders);
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}