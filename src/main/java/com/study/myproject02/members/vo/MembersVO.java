package com.study.myproject02.members.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MembersVO {
    private String m_idx, m_id, m_pw, m_name, m_addr,  m_addr2, m_email, m_phone, m_reg,
    m_active, m_active_reg, sns_email_naver, sns_email_kakao, sns_provider ;
}
