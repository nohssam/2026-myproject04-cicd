package com.study.myproject02.members.service;

import com.study.myproject02.config.OAuthConfig.NaverOAuthProperties;
import com.study.myproject02.members.vo.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverOAuthService {

    private final WebClient webClient;
    private final NaverOAuthProperties naverProperties;

    /**
     * 네이버 OAuth 인가 코드로 Access Token 교환
     */
    public String getAccessToken(String code, String state) {
        try {
            Map<String, Object> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("nid.naver.com")
                            .path("/oauth2.0/token")
                            .queryParam("grant_type", "authorization_code")
                            .queryParam("client_id", naverProperties.getClientId())
                            .queryParam("client_secret", naverProperties.getClientSecret())
                            .queryParam("code", code)
                            .queryParam("state", state)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("access_token")) {
                return (String) response.get("access_token");
            }

            log.error("네이버 토큰 발급 실패: {}", response);
            return null;
        } catch (Exception e) {
            log.error("네이버 토큰 요청 중 오류: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Access Token으로 네이버 사용자 정보 조회
     */
    public OAuthUserInfo getUserInfo(String accessToken) {
        try {
            Map<String, Object> response = webClient.get()
                    .uri(naverProperties.getUserInfoUri())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && "00".equals(response.get("resultcode"))) {
                Map<String, Object> userResponse = (Map<String, Object>) response.get("response");
                // 네이버는 mobile
                String rawPhone = (String) userResponse.get("mobile");
                // +82-10-1234-5678  -> 010-1234-5678
                String phone = rawPhone != null ? rawPhone.replaceFirst("\\+82-?", "0") : "010-1111-1111";
                return OAuthUserInfo.builder()
                        .provider("naver")
                        .providerId((String) userResponse.get("id"))
                        .email((String) userResponse.get("email"))
                        .name((String) userResponse.get("name"))
                        .profileImage((String) userResponse.get("profile_image"))
                        .phone(phone)
                        .build();
            }

            log.error("네이버 사용자 정보 조회 실패: {}", response);
            return null;
        } catch (Exception e) {
            log.error("네이버 사용자 정보 요청 중 오류: {}", e.getMessage());
            return null;
        }
    }
}
