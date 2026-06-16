package com.study.myproject02.members.service;

import com.study.myproject02.config.OAuthConfig.KakaoOAuthProperties;
import com.study.myproject02.members.vo.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final WebClient webClient;
    private final KakaoOAuthProperties kakaoProperties;

    /**
     * 카카오 OAuth 인가 코드로 Access Token 교환
     */
    public String getAccessToken(String code) {
        try {
            Map<String, Object> response = webClient.post()
                    .uri(kakaoProperties.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters
                            .fromFormData("grant_type", "authorization_code")
                            .with("client_id", kakaoProperties.getClientId())
                            .with("client_secret", kakaoProperties.getClientSecret() != null ? kakaoProperties.getClientSecret() : "")
                            .with("redirect_uri", kakaoProperties.getRedirectUri())
                            .with("code", code))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("access_token")) {
                return (String) response.get("access_token");
            }

            log.error("카카오 토큰 발급 실패: {}", response);
            return null;
        } catch (Exception e) {
            log.error("카카오 토큰 요청 중 오류: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Access Token으로 카카오 사용자 정보 조회
     */
    public OAuthUserInfo getUserInfo(String accessToken) {
        try {
            Map<String, Object> response = webClient.get()
                    .uri(kakaoProperties.getUserInfoUri())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("id")) {
                String id = String.valueOf(response.get("id"));

                Map<String, Object> kakaoAccount = (Map<String, Object>) response.get("kakao_account");
                Map<String, Object> profile = kakaoAccount != null ?
                        (Map<String, Object>) kakaoAccount.get("profile") : null;

                String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
                String name = profile != null ? (String) profile.get("nickname") : null;
                String profileImage = profile != null ? (String) profile.get("profile_image_url") : null;
                String rawPhone = kakaoAccount != null ? (String) kakaoAccount.get("phone-number") : null;

                // +82-10-1234-5678  -> 010-1234-5678
                String phone = rawPhone != null ? rawPhone.replaceFirst("\\+82-?", "0") : "010-1111-1111";

                return OAuthUserInfo.builder()
                        .provider("kakao")
                        .providerId(id)
                        .email(email)
                        .name(name)
                        .profileImage(profileImage)
                        .phone(phone)
                        .build();
            }

            log.error("카카오 사용자 정보 조회 실패: {}", response);
            return null;
        } catch (Exception e) {
            log.error("카카오 사용자 정보 요청 중 오류: {}", e.getMessage());
            return null;
        }
    }
}
