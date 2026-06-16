package com.study.myproject02.guestbook.service;

import com.study.myproject02.guestbook.vo.GuestBookVO;

import java.util.List;

public interface GuestBookService {
    List<GuestBookVO> guestBookList();
    int guestBookInsert(GuestBookVO gvo);
    int guestBookUpdate(GuestBookVO gvo);
    int guestBookDelete(GuestBookVO gvo);
}
