package com.utgaming.testconcurrency.controller;

import com.utgaming.testconcurrency.entity.login.LoginUserInfo;
import com.utgaming.testconcurrency.common.Result;
import com.utgaming.testconcurrency.entity.login.RegisterUserInfo;
import com.utgaming.testconcurrency.service.AuthService;
import com.utgaming.testconcurrency.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final JwtUtil jwtUtil;
    @Autowired
    private final AuthService authService;

    public AuthController(JwtUtil jwtUtil, AuthService authService) {
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result login(@RequestBody LoginUserInfo loginUserInfo) {
        return Result.success(authService.login(loginUserInfo));

/*        if (userInfo == null) return Result.error(400,"参数为空",null);

        String username = userInfo.getUsername();

        if(!"admin".equals(username)) return Result.error(401,"账号密码错误",null);

        String token = jwtUtil.generateToken(userInfo.getUserId(), username);

        return Result.success(token);*/
    }

    @PostMapping("/register")
    public Result register(@RequestBody RegisterUserInfo info) {
        return Result.success(authService.register(info));
    }
}
