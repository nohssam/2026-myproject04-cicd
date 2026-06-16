package com.study.myproject02.guestbook.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuestBookVO {
    private String g_idx, g_writer, g_subject, g_email, g_pwd, g_content, g_regdate, g_active, f_name;
    private MultipartFile file_name;
}
