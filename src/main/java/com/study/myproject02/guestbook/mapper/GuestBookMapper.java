package com.study.myproject02.guestbook.mapper;

import com.study.myproject02.guestbook.vo.GuestBookVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GuestBookMapper {
    List<GuestBookVO> guestBookList();
    int guestBookInsert(GuestBookVO gvo);
    GuestBookVO guestBookDetail(String g_idx);
    int guestBookUpdate(GuestBookVO gvo);
    int guestBookDelete(GuestBookVO gvo);
}
