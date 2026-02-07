package com.utgaming.testconcurrency.entity.login;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginUserInfo {
    String username;
    String password;

    public LoginUserInfo(String username,String password) {
        this.username = username;
        this.password = password;
    }
}
