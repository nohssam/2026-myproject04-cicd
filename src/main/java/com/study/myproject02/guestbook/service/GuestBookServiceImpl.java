package com.study.myproject02.guestbook.service;

import com.study.myproject02.guestbook.mapper.GuestBookMapper;
import com.study.myproject02.guestbook.vo.GuestBookVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class GuestBookServiceImpl implements GuestBookService{
    @Autowired
    private GuestBookMapper guestBookMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<GuestBookVO> guestBookList() {
        return guestBookMapper.guestBookList();
    }

    @Override
    public int guestBookInsert(GuestBookVO gvo) {
        // 비밀번호 암호화 추가 하자
        gvo.setG_pwd(passwordEncoder.encode(gvo.getG_pwd()));
        return guestBookMapper.guestBookInsert(gvo);
    }

    @Override
    public int guestBookUpdate(GuestBookVO gvo) {
        // 비밀번호 체크
        GuestBookVO current = guestBookMapper.guestBookDetail(gvo.getG_idx());
        if(current == null || !passwordEncoder.matches(gvo.getG_pwd(), current.getG_pwd())){
          throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        return guestBookMapper.guestBookUpdate(gvo);
    }

    @Override
    public int guestBookDelete(GuestBookVO gvo) {
        // 비밀번호 체크
        GuestBookVO current = guestBookMapper.guestBookDetail(gvo.getG_idx());
        if(current == null || !passwordEncoder.matches(gvo.getG_pwd(), current.getG_pwd())){
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        return guestBookMapper.guestBookDelete(gvo);
    }
}
