package com.utgaming.testconcurrency.entity.login;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAccount {
    @TableId(type = IdType.AUTO)
    Long id;
    String username;
    @TableField("password_hash")
    String passwordHash;
    LocalDateTime createdAt;
}
