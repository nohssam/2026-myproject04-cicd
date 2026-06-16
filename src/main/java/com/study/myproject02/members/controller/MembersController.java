package com.study.myproject02.members.controller;

import com.study.myproject02.common.jwt.JwtUtil;
import com.study.myproject02.common.vo.DataVO;
import com.study.myproject02.members.service.KakaoOAuthService;
import com.study.myproject02.members.service.MembersService;
import com.study.myproject02.members.service.NaverOAuthService;
import com.study.myproject02.members.vo.MembersVO;
import com.study.myproject02.members.vo.OAuthUserInfo;
import com.study.myproject02.members.vo.RefreshTokenVO;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/members")
public class MembersController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MembersService membersService;

    @Autowired
    private NaverOAuthService naverOAuthService;

    @Autowired
    private KakaoOAuthService kakaoOAuthService;

    @GetMapping("/hello")
    public String getHello(){
        return "Hello World! ";
    }

    @PostMapping("/hi")
    public String getHi(){
        return "Bye World! ";
    }

    @GetMapping("/hello2")
    public String getHello2(@RequestParam String msg){
        return  msg + "님 Hello World! ";
    }

    @PostMapping("/hi2")
    public String getHi2(@RequestBody Map<String, String> body){
        return body.get("msg")+"님 Hi World! ";
    }

    @PostMapping("/register")
    public DataVO getRegister(@RequestBody MembersVO mvo){
        DataVO dataVO = new DataVO();
        try{
        // 비빌번호 암호화 하기
            mvo.setM_pw(passwordEncoder.encode(mvo.getM_pw()));

         // DB에 insert 하기
         membersService.register(mvo);

         dataVO.setSuccess(Boolean.TRUE);
         dataVO.setMessage("회원가입 성공");

        }catch (Exception e){
            dataVO.setSuccess(false);
            dataVO.setMessage("서버 오류 : " +e.getMessage());
        }
        return dataVO;
    }

    @PostMapping("/login")
    public DataVO getLogin(@RequestBody MembersVO mvo){
        DataVO dataVO = new DataVO();
        try{
            // 아이디 존재 여부 확인
            MembersVO membersVO = membersService.findById(mvo.getM_id());

            if(membersVO == null){
                dataVO.setSuccess(Boolean.FALSE);
                dataVO.setMessage("없는 아이디 입니다");
                return dataVO;
            }
            // 비밀번호 검증
            if(!passwordEncoder.matches(mvo.getM_pw(),membersVO.getM_pw())){
                dataVO.setSuccess(Boolean.FALSE);
                dataVO.setMessage("비밀번호가 틀렸습니다.");
                return dataVO;
            }

            // 토큰 생성
            String accessToken = jwtUtil.generateAccessToken(membersVO.getM_id());
            String refreshToken = jwtUtil.generateRefreshToken(membersVO.getM_id());

            // 기존 refresh token 삭제 후  새 토큰 저장 (중복 로그인 방지/ 항상 최신 토그만 유지)
            membersService.deleteRefreshToken(membersVO.getM_id());

            // 새로 만들어진 refresh token 저장
            RefreshTokenVO  refreshTokenVO = new RefreshTokenVO();
            refreshTokenVO.setRt_user_id(membersVO.getM_id());
            refreshTokenVO.setRt_token(refreshToken);
            membersService.saveRefreshToken(refreshTokenVO);

            // 클라이언트에게 보낼 정보 저장
            Map<String,Object> map = new HashMap<>();
            map.put("accessToken",accessToken);
            map.put("refreshToken",refreshToken);
            map.put("membersVO",membersVO);

            // 클라이언트에게 정보 보내기
            dataVO.setSuccess(true);
            dataVO.setMessage("로그인 성공");
            dataVO.setData(map);

        } catch (Exception e) {
            dataVO.setSuccess(Boolean.FALSE);
            dataVO.setMessage("서버 오류 : " + e.getMessage());
        }

        return  dataVO;
    }

    // mypage : 필터가 이미 토큰 검증 완료 -> SecurityContextHolder에서 userId 바로 꺼냄
    @GetMapping("/myPage")
    public DataVO getMyPage(){
        DataVO dataVO = new DataVO();
        try{
            String userId = (String)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            // log.info("userid : {}", userId);
            MembersVO mvo = membersService.findById(userId);
            if(mvo == null){
                dataVO.setSuccess(Boolean.FALSE);
                dataVO.setMessage("없는 아이디 입니다.");
            }else{
                dataVO.setSuccess(Boolean.TRUE);
                dataVO.setMessage("마이페이지 성공");
                dataVO.setData(mvo);
            }
        } catch (Exception e) {
           dataVO.setSuccess(Boolean.FALSE);
           dataVO.setMessage(e.getMessage());
        }
        return dataVO;
    }

    // accessToken 이 만료 되어 클라이언트에서  refreshToken을 보내면 확인 후
    // accessToken과 refreshToken을 새로 생성 (refreshToken는 DB  저장)
    // 요청 body: {"refreshToken: "로그인 시 받은 refreshToken"}
    @PostMapping("/refresh")
    public DataVO getRefreshToken(@RequestBody Map<String, String> body){
        DataVO dataVO = new DataVO();
        try{
            // 1) refreshToken 추출
            String refreshToken = body.get("refreshToken");
            log.info("refreshToken: {}",refreshToken);

            // 2) 빈값 체크 : refreshTopken을 body엥 담지 않은 경우
            if(refreshToken == null ||  refreshToken.isBlank()){
                dataVO.setSuccess(Boolean.FALSE);
                dataVO.setMessage("refreshToken이 없네요");
                return dataVO;
            }

            // 3) DB에서 저장된 토큰 확인
            RefreshTokenVO storeToken = membersService.findRefreshToken(refreshToken);
            if(storeToken == null){
                dataVO.setSuccess(Boolean.FALSE);
                dataVO.setMessage("유효하지 않는 refreshToken 입니다.");
                return dataVO;
            }

            // 이전 AccessToken으로 들어오면 체크 ?
            // 4) JWT 검증 (서명+만료)
            String userId = jwtUtil.validateToken(refreshToken);
            if(userId == null){
                dataVO.setSuccess(Boolean.FALSE);
                dataVO.setMessage("유효하지 않는 refreshToken 입니다.");
                return dataVO;
            }

            // 5) 새 토큰 생성 (둘 다)
            String newAccessToken = jwtUtil.generateAccessToken(userId);
            String newRefreshToken = jwtUtil.generateRefreshToken(userId);

            // 6) 토큰 로테이션: 기존 refreshToken 삭제 후 새로운 refreshToken 저장
            membersService.deleteRefreshToken(userId);
            RefreshTokenVO  newToken = new RefreshTokenVO();
            newToken.setRt_user_id(userId);
            newToken.setRt_token(newRefreshToken);
            membersService.saveRefreshToken(newToken);

            // 7) 새 토큰을 클라이어트에게 보내다.
            Map<String,Object> map = new HashMap<>();
            map.put("accessToken",newAccessToken);
            map.put("refreshToken",newRefreshToken);

            dataVO.setSuccess(true);
            dataVO.setMessage("재발급 성공");
            dataVO.setData(map);
            log.info("refreshToken 발급성공");

        } catch (ExpiredJwtException e) {
            // refreshToken 만료 시 여기로 이동
            // DB에서 삭제 후 재로그인 유도 (refreshToken 까지 만료 되면 재 로그인 해야 됨)
            // String userId = (String)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userId = e.getClaims().getSubject();
            membersService.deleteRefreshToken(userId);
            dataVO.setSuccess(Boolean.FALSE);
            dataVO.setMessage("refreshToken 만료, 다시 로그인 해주세요");
        } catch (Exception e) {
            dataVO.setSuccess(Boolean.FALSE);
            dataVO.setMessage("refreshToken 오류");
        }
        return  dataVO;
    }

    // 로그아웃 : DB에서 refreshToken 삭제(accessToken은 클라이언트에서 삭제)
    @PostMapping("/logout")
    public DataVO getLogout(){
        log.info("getLogout 들어옴");
        DataVO dataVO = new DataVO();
        try{
            String userId = (String)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            log.info("id : {}", userId);
            membersService.deleteRefreshToken(userId);

            dataVO.setSuccess(Boolean.TRUE);
            dataVO.setMessage("로그아웃 성공");
            log.info("로그아웃 성공");
        } catch (Exception e) {
            dataVO.setSuccess(Boolean.FALSE);
            dataVO.setMessage("로그아웃 실패");
            log.info("로그아웃 실패");
        }
        return dataVO;
    }

    // 회원탈퇴
    @GetMapping("/delAccount")
    public DataVO getDelAccount(){
        DataVO dataVO = new DataVO();
        try{
            String userId = (String)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            log.info("id : {}", userId);
            membersService.deleteRefreshToken(userId);
            membersService.deleteAccount(userId);

            dataVO.setSuccess(Boolean.TRUE);
            dataVO.setMessage("회원탈퇴 성공");
            log.info("회원탈퇴 성공");

        }catch (Exception e){
            dataVO.setSuccess(Boolean.FALSE);
            dataVO.setMessage("회원탈퇴 실패");
            log.info("회원탈퇴 실패");
        }
        return dataVO;
    }

    @PostMapping("/updateMember")
    public DataVO updateMember(@RequestBody MembersVO mvo){
        DataVO dataVO = new DataVO();
        try {
            String userId = (String)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            mvo.setM_id(userId);
            membersService.updateMember(mvo);

            dataVO.setSuccess(Boolean.TRUE);
            dataVO.setMessage("회원수정 성공");
            log.info("회원수정 성공");
        } catch (Exception e) {
            dataVO.setSuccess(Boolean.FALSE);
            dataVO.setMessage("회원수정 실패");
            log.info("회원수정 실패");
        }
        return dataVO;
    }
    @GetMapping("/checkId")
    public DataVO getCheckId(@RequestParam String m_id){
        DataVO dataVO = new DataVO();
        try{
            int count = membersService.checkId(m_id);
            log.info("count : {}", count);
            if(count == 0){
                dataVO.setSuccess(Boolean.TRUE);
                dataVO.setMessage("사용 가능한 아이디 입니다");
            }else{
                dataVO.setSuccess(Boolean.FALSE);
                dataVO.setMessage("이미 사용중인 아이디 입니다");
            }
        } catch (Exception e) {
            log.info("서버 오류 : {}" , e.getMessage() );
            dataVO.setSuccess(Boolean.FALSE);
            dataVO.setMessage("서버 오류 : " + e.getMessage()   );
        }
        return dataVO;
    }

    // ==================== OAuth 소셜 로그인 ====================

    /**
     * 네이버 OAuth 콜백 처리
     * 프론트에서 code, state를 받아 네이버 API로 토큰 교환 후 사용자 정보 조회
     */
    @PostMapping("/oauth/naver")
    public DataVO naverOAuthCallback(@RequestBody Map<String, String> body) {
        DataVO dataVO = new DataVO();
        try {
            String code = body.get("code");
            String state = body.get("state");

            if (code == null || code.isBlank()) {
                dataVO.setSuccess(Boolean.FALSE);
                dataVO.setMessage("인가 코드가 없습니다.");
                return dataVO;
            }

            // 1) 네이버에서 Access Token 받기
            String accessToken = naverOAuthService.getAccessToken(code, state);
            if (accessToken == null) {
                dataVO.setSuccess(Boolean.FALSE);
                dataVO.setMessage("네이버 토큰 발급에 실패했습니다.");
                return dataVO;
            }

            // 2) 네이버 사용자 정보 조회
            OAuthUserInfo userInfo = naverOAuthService.getUserInfo(accessToken);
            if (userInfo == null || userInfo.getEmail() == null) {
                dataVO.setSuccess(Boolean.FALSE);
                dataVO.setMessage("네이버 사용자 정보를 가져올 수 없습니다.");
                return dataVO;
            }

            // 3) OAuth 로그인 처리 (회원 조회/가입 + JWT 발급)
            return processOAuthLogin(userInfo);

        } catch (Exception e) {
            log.error("네이버 OAuth 오류: {}", e.getMessage());
            dataVO.setSuccess(Boolean.FALSE);
            dataVO.setMessage("네이버 로그인 처리 중 오류가 발생했습니다.");
        }
        return dataVO;
    }

    /**
     * 카카오 OAuth 콜백 처리
     * 프론트에서 code를 받아 카카오 API로 토큰 교환 후 사용자 정보 조회
     */
    @PostMapping("/oauth/kakao")
    public DataVO kakaoOAuthCallback(@RequestBody Map<String, String> body) {
        DataVO dataVO = new DataVO();
        try {
            String code = body.get("code");

            if (code == null || code.isBlank()) {
                dataVO.setSuccess(Boolean.FALSE);
                dataVO.setMessage("인가 코드가 없습니다.");
                return dataVO;
            }

            // 1) 카카오에서 Access Token 받기
            String accessToken = kakaoOAuthService.getAccessToken(code);
            if (accessToken == null) {
                dataVO.setSuccess(Boolean.FALSE);
                dataVO.setMessage("카카오 토큰 발급에 실패했습니다.");
                return dataVO;
            }

            // 2) 카카오 사용자 정보 조회
            OAuthUserInfo userInfo = kakaoOAuthService.getUserInfo(accessToken);
            if (userInfo == null) {
                dataVO.setSuccess(Boolean.FALSE);
                dataVO.setMessage("카카오 사용자 정보를 가져올 수 없습니다.");
                return dataVO;
            }

            // 카카오는 이메일이 없을 수 있으므로 providerId 사용
            //   if (userInfo.getEmail() == null) {
            //    userInfo.setEmail(userInfo.getProviderId() + "@kakao.user");
            //  }

            // 3) OAuth 로그인 처리 (회원 조회/가입 + JWT 발급)
            return processOAuthLogin(userInfo);

        } catch (Exception e) {
            log.error("카카오 OAuth 오류: {}", e.getMessage());
            dataVO.setSuccess(Boolean.FALSE);
            dataVO.setMessage("카카오 로그인 처리 중 오류가 발생했습니다.");
        }
        return dataVO;
    }

    /**
     * OAuth 로그인 공통 처리
     * - SNS 이메일로 기존 회원 조회
     * - 없으면 자동 회원가입
     * - JWT 토큰 발급 및 반환
     */
    private DataVO processOAuthLogin(OAuthUserInfo userInfo) {
        DataVO dataVO = new DataVO();
        String provider = userInfo.getProvider();
        String email = userInfo.getEmail();

        // 1) SNS 이메일로 기존 회원 조회
        MembersVO membersVO = membersService.findBySnsEmail(provider, email);

        // 2) 회원이 없으면 자동 가입
        if (membersVO == null) {
            // 일반 가입  이메일로 기존 회원 조회 -> 계정 연동
            membersVO = membersService.findByEmail(email);

            if (membersVO != null) {
                // 기존 일반 회원에 SNS  정보 추가
                membersVO.setSns_provider(provider);
                if ("naver".equals(provider)) {
                    membersVO.setSns_email_naver(email);
                } else if ("kakao".equals(provider)) {
                    membersVO.setSns_email_kakao(email);
                }
                membersService.updateSnsInfo(membersVO);
                log.info("기존 SNS 정보 연동 : {} ", userInfo.getProviderId());

            }else{
                // 고유 ID 생성: provider_providerId (예: naver_12345678)
                String uniqueId = provider + "_" + userInfo.getProviderId();

                // m_id로 기존 SNS  회원조회 (sns_email이 비어 있는 경우 대비)
                membersVO = membersService.findById(uniqueId);

                if(membersVO != null){
                    // sns_email 컬럼이 비어있던 기존 회원 -> SnS  정보 보완
                    membersVO.setSns_provider(provider);
                    if ("naver".equals(provider)) {
                        membersVO.setSns_email_naver(email);
                    } else if ("kakao".equals(provider)) {
                        membersVO.setSns_email_kakao(email);
                    }
                    membersService.updateSnsInfo(membersVO);
                    log.info("기존 SNS 정보 업데이트 : {} ", uniqueId);
                }else{
                    // 신규 SNS 회원 생성
                    membersVO = new MembersVO();
                    membersVO.setM_id(uniqueId);
                    membersVO.setM_name(userInfo.getName()!=null?userInfo.getName():"SNS 사용자");
                    membersVO.setM_email(email);
                    membersVO.setM_phone(userInfo.getPhone() != null ? userInfo.getPhone():"-");
                    membersVO.setSns_provider(provider);
                    if ("naver".equals(provider)) {
                        membersVO.setSns_email_naver(email);
                    } else if ("kakao".equals(provider)) {
                        membersVO.setSns_email_kakao(email);
                    }
                    // 프론트엔드에서 OAuth 요청을 2번 보내고 있습니다:
                    // 1. 첫 번째 요청 (exec-4): 회원가입 + 토큰 저장 성공
                    // 2. 두 번째 요청 (exec-3): 이미 회원이 있는데 다시 INSERT 시도 → Duplicate entry 에러
                    // 두 번째 요청에서 에러가 나면서 refresh token 저장 전에 예외가 발생
                    try {
                        membersService.registerSnsUser(membersVO);
                        log.info("SNS 신규 회원 가입 {} ", uniqueId);
                    } catch (Exception e) {
                        // 중복 요청으로 이미 가입된 경우 기존 회원 조회
                        log.info("이미 가입된 회원, 기존 정보 조회 {} ", uniqueId);
                        membersVO = membersService.findById(uniqueId);
                    }
                }
            }
         }

        // 3) JWT 토큰 생성
        return generateTokenResponse(membersVO);
    }

    /**
     * JWT 토큰 응답 생성 (로그인/OAuth 공통)
     */
    private DataVO generateTokenResponse(MembersVO membersVO) {
        DataVO dataVO = new DataVO();

        String accessToken = jwtUtil.generateAccessToken(membersVO.getM_id());
        String refreshToken = jwtUtil.generateRefreshToken(membersVO.getM_id());

        // 기존 refresh token 삭제 후 새 토큰 저장
        membersService.deleteRefreshToken(membersVO.getM_id());
        log.info("토큰 삭제");

        RefreshTokenVO refreshTokenVO = new RefreshTokenVO();
        refreshTokenVO.setRt_user_id(membersVO.getM_id());
        refreshTokenVO.setRt_token(refreshToken);
        membersService.saveRefreshToken(refreshTokenVO);
        log.info("토큰 저장");
        // 클라이언트에게 보낼 정보
        Map<String, Object> map = new HashMap<>();
        map.put("accessToken", accessToken);
        map.put("refreshToken", refreshToken);
        map.put("membersVO", membersVO);

        dataVO.setSuccess(true);
        dataVO.setMessage("로그인 성공");
        dataVO.setData(map);

        log.info("로그인 성공~~~~~~");

        return dataVO;
    }
}
