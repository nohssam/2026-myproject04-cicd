package com.study.myproject02.members.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuthUserInfo {
    private String provider;      // "naver" or "kakao"
    private String providerId;    // SNS 서비스에서 제공하는 고유 ID
    private String email;         // SNS 이메일
    private String name;          // 사용자 이름
    private String phone;         // 전화번호
    private String profileImage;  // 프로필 이미지 URL
}
