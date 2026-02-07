package com.utgaming.testconcurrency.service;

import com.utgaming.testconcurrency.entity.login.LoginUserInfo;
import com.utgaming.testconcurrency.entity.login.RegisterUserInfo;
import com.utgaming.testconcurrency.entity.login.UserInfo;

public interface AuthService {
    String login(LoginUserInfo loginUserInfo);

    UserInfo register(RegisterUserInfo info);
}
