package com.study.myproject02.common.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// OncePerRequestFilter : 하나의 요청당 딱 한번만 실행 되는 필터
// 모든 요청은 컨트롤러에 도달하기 전에 반드시 이 필터를 통과한다.
@Slf4j
@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("JwtRequestFilter  실행");

        // 1) Authorization 헤더 추출
        // "Authorization: Bearer 토큰" 형식
        String authHeader = request.getHeader("Authorization");
        log.info("authHeader : {}", authHeader);
        // 2) 헤더가 없거나 Bearer 형식이 아니면 그냥 통과
        // -/members/login,  /members/refresh 같은 공개 엔드포인트는 토큰 없이 요청하므로 여기서 통과
        // 이후 SecurityConfig에서 권한 체크를 담당
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3) "Bearer 토큰"형식 => 앞 Bearer (7자리)제거 해야 순수 토큰이다.
        String token = authHeader.substring(7);
        log.info("token 체크 : {}", token);

        // 4) 토큰 검증
        try{
            String userId = jwtUtil.validateToken(token);
            if(userId != null) {
                // 5) 토큰 유효 => SecurityContextHolder에 인증 정보 등록
                // SecurityContextHolder : 현재 로그인 한 사용자 정보를 애플리케이션 어디서나 꺼내 쓸수 있게 보관하는 전역 저장소
                //                         컨틀로러/서비스에서 꺼내 사용
                UsernamePasswordAuthenticationToken authenticationToken
                        = new UsernamePasswordAuthenticationToken(userId, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }else{
               // 6) 토큰 위조,형식 오류 -> 401 반환, 컨트롤러로 진행하지 않음
                log.error("토큰 검증 실패");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false, \"message\":\"token invalid\"}");
                return ;
            }
        }catch (ExpiredJwtException e) {
            // 7) 토큰 만료 -> 401
            // 클라이언트는  이 응답을 받으면 POST /members/refresh 로 재발급 요청
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false, \"message\":\"token expired\"}");
            return ;
        }catch (Exception e) {
            // 8) 기타 예외 상황
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false, \"message\":\"token invalid\"}");
            return ;
        }

        // 9) 토큰 검증 완료 -> 다음 필터 또는 컨트롤러로 진행
         filterChain.doFilter(request, response);
    }
}












