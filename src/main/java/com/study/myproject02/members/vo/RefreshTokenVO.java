package com.study.myproject02.members.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenVO {
    private String rt_idx, rt_user_id, rt_token, rt_reg ;
}
