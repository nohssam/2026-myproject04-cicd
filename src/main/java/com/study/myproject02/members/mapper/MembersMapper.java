package com.study.myproject02.members.mapper;

import com.study.myproject02.members.vo.MembersVO;
import com.study.myproject02.members.vo.RefreshTokenVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MembersMapper {
    // 회원가입
    void register(MembersVO mvo);

    // 회원탈퇴
    void deleteAccount(String userId);

    // 회원정보 수정
    void updateMember(MembersVO mvo);

    // 아이디를 받아서 아이디가 있는지 확인
    MembersVO findById(String id);

    // refreshToken 관련
    void deleteRefreshToken(String id);

    // 새로 만들어진 refresh token 저장
    void saveRefreshToken(RefreshTokenVO refreshTokenVO);

    // refreshToken을 받아서 DB에서 있는지 찾기
    RefreshTokenVO findRefreshToken(String refreshToken);

    // 아이디 검사
    int checkId(String m_id);

    // SNS OAuth 관련
    // 일반 이메일로 회원 조회(SNS 연동 시 기존 계정 확인)
    MembersVO findByEmail(String email);

    // SNS 이메일로 회원 조회
    MembersVO findBySnsEmail(@Param("provider") String provider, @Param("email") String email);

    // SNS 회원 등록 (비밀번호 없이)
    void registerSnsUser(MembersVO mvo);

    // 기존 회원에 SNS 정보 연동
    void updateSnsInfo(MembersVO mvo);
}
