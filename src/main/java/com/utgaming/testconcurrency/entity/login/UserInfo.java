package com.utgaming.testconcurrency.entity.login;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;


@Data
public class UserInfo {
    @TableId(type = IdType.AUTO)
    Long userId;
    String username;
    public UserInfo(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }
}
