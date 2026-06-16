package com.study.myproject02.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

// OAuth 로긍니에 필요한 설정 값들을 한 곳에 모아둔 설정 객체
@Configuration
public class OAuthConfig {

    // WebClient ; 외부 API 호출용 HTTP 클라이언트
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    // application.yaml에서 바인딩
    @Bean
    @ConfigurationProperties(prefix = "oauth.naver")
    public NaverOAuthProperties naverOAuthProperties() {
        return new NaverOAuthProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "oauth.kakao")
    public KakaoOAuthProperties kakaoOAuthProperties() {
        return new KakaoOAuthProperties();
    }

    // 네이버 설정 클래스
    @Getter
    @Setter
    public static class NaverOAuthProperties {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String tokenUri;
        private String userInfoUri;
    }

    // 카카오 설정 클래스
    @Getter
    @Setter
    public static class KakaoOAuthProperties {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String tokenUri;
        private String userInfoUri;
    }
}
