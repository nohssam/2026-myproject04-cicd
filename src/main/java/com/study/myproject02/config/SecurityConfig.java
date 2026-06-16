package com.study.myproject02.config;

import com.study.myproject02.common.jwt.JwtRequestFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

// Configuration : 설정 클래스 (Spring boot 실행 될 때 같이 실행 된다.)
// EnableWebSecurity : Spring Security에서 웹 보안 설정을 활성화 하는 어노테이션
@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    // 생성자에 JwtRequestFilter 를 주입
    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }



    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        log.info("securityFilterChain 시작");
        http
                // 1. CORS 설정 (corsConfigurationSource() 별도로 생성
                // CORS를 설정한다는 건 ‘출처가 다른 서버 간의 리소스 공유’를 허용한다는 거죠.(서버와 클라이언트간의 출쳐가 다른다.)
                // 서로 다른 출처일 때 리소스 요청과 응답을 차단하는 정책 (서로 다른 출처일 때 리소스 요청과 응답을 차단하는 정책)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // CSRF 보호 비활성 ( JWT 토큰 사용 시 일반적으로 비활성
                // CSRF : 사용자가 로그인 된 상태를 악용하여, 악의적인 사이트가 사용자의 권한으로 요청을 보내도록 만드는 공격
                //         JWT 를 사용하면 CSRF의 위헙이 없음
                .csrf(csrf -> csrf.disable())
                // Spring Security의 세션 관리 필터 자체가 제거
                //.sessionManagement(sessionManagement -> sessionManagement.disable())
                // 모듈은 살아 있음 , 세션 생성 안함, JWT 사용 시 권장
                .sessionManagement(session ->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 요청별 권한 설정
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        // 허용한 URL만 통과 시킨다.
                        // /members/로 들어오는 모든 것 허용
                        //.requestMatchers("/members/**").permitAll()
                        .requestMatchers("/members/login", "/members/register", "/members/refresh", "/members/checkId",
                                "/members/oauth/naver", "/members/oauth/kakao").permitAll()
                        // 방명록 목록은 비로그인도 접근 가능, 나머지는 인증 필요
                        // get, post ... 모든 옵션 허용
                        // .requestMatchers("/guestbook/list").permitAll()
                        // get만 허용 ("POST", "PUT", "DELETE",  허용 안함)
                        .requestMatchers(HttpMethod.GET, "/guestbook/list").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(e-> e
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"success\":false, \"message\":\"인증이 필요합니다.\"}");
                        })
                )

                // JWT 필터
                // 사용자 요청이 오면 jwtRequestFilter가 실행되어 JWT 토큰을 검증한 후
                // 이상이 없으면 다음 필터로 진행
                // UsernamePasswordAuthenticationFilter 보다 앞에 삽입
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration  corsConfiguration = new CorsConfiguration();

        // 허용할 Origin Origin, 메서드, 헤더 , 인증
        // 리액트
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        corsConfiguration.setAllowedOrigins(origins);
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
        corsConfiguration.setAllowCredentials(true);

        // URL 패턴 기반으로 CORS 설정을 관리하는 저장소 생성
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();

        // 모든  URL 경로에 적용, CORS 규칙 객체
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return urlBasedCorsConfigurationSource;
    }
}
