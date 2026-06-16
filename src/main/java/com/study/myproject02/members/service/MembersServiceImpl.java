package com.study.myproject02.members.service;

import com.study.myproject02.members.mapper.MembersMapper;
import com.study.myproject02.members.vo.MembersVO;
import com.study.myproject02.members.vo.RefreshTokenVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MembersServiceImpl implements MembersService {
    @Autowired
    private MembersMapper membersMapper;

    @Override
    public void register(MembersVO mvo) { membersMapper.register(mvo);}

    @Override
    public MembersVO findById(String id) {
        return membersMapper.findById(id);
    }

    @Override
    public void deleteAccount(String userId) { membersMapper.deleteAccount(userId); }

    @Override
    public void updateMember(MembersVO mvo) { membersMapper.updateMember(mvo);  }

    @Override
    public void deleteRefreshToken(String id) {
       membersMapper.deleteRefreshToken(id);
    }

    @Override
    public void saveRefreshToken(RefreshTokenVO refreshTokenVO) {
        membersMapper.saveRefreshToken(refreshTokenVO);
    }

    @Override
    public RefreshTokenVO findRefreshToken(String refreshToken) {
        return membersMapper.findRefreshToken(refreshToken);
    }

    @Override
    public int checkId(String m_id) {
        return membersMapper.checkId(m_id);
    }

    // SNS OAuth 관련
    // 일반 이메일로 회원 조회(SNS 연동 시 기존 계정 확인
    @Override
    public MembersVO findByEmail(String email) {
        return membersMapper.findByEmail(email);
    }

    // SNS 이메일로 회원 조회
    @Override
    public MembersVO findBySnsEmail(String provider, String email) {
        return membersMapper.findBySnsEmail(provider, email);
    }

    // SNS 회원 등록 (비밀번호 없이)
    @Override
    public void registerSnsUser(MembersVO mvo) {
        membersMapper.registerSnsUser(mvo);
    }

    // 기존 회원에 SNS 정보 연동
    @Override
    public void updateSnsInfo(MembersVO mvo) {
        membersMapper.updateSnsInfo(mvo);
    }
}
