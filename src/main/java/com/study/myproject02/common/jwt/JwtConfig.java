package com.study.myproject02.common.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
    // application.yaml에 존재하는 값 가져온다.
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(secret, accessTokenValidity, refreshTokenValidity);
    }

}
