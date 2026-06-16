package com.study.myproject02.members.service;

import com.study.myproject02.members.vo.MembersVO;
import com.study.myproject02.members.vo.RefreshTokenVO;

public interface MembersService {

    // 회원가입
    void register(MembersVO mvo);
    MembersVO findById(String id);
    // 회원 탈퇴
    void deleteAccount(String userId);
    // 회원 수정
    void updateMember(MembersVO mvo);

    // refreshToken 관련
    void deleteRefreshToken(String id);
    // 새로 만들어진 refresh token 저장
    void saveRefreshToken(RefreshTokenVO refreshTokenVO);
    // refreshToken을 받아서 DB에서 있는지 찾기
    RefreshTokenVO findRefreshToken(String refreshToken);

    // 아이디 검사
    int checkId(String m_id);

    // SNS OAuth 관련
    MembersVO findBySnsEmail(String provider, String email);
    MembersVO findByEmail(String email);
    void registerSnsUser(MembersVO mvo);
    void updateSnsInfo(MembersVO mvo);
}
