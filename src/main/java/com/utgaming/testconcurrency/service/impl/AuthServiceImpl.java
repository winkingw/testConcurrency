package com.utgaming.testconcurrency.service.impl;

import com.utgaming.testconcurrency.entity.login.LoginUserInfo;
import com.utgaming.testconcurrency.common.UnauthorizedException;
import com.utgaming.testconcurrency.entity.login.RegisterUserInfo;
import com.utgaming.testconcurrency.entity.login.UserAccount;
import com.utgaming.testconcurrency.entity.login.UserInfo;
import com.utgaming.testconcurrency.mapper.UserAccountMapper;
import com.utgaming.testconcurrency.service.AuthService;
import com.utgaming.testconcurrency.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCrypt;


@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    UserAccountMapper userAccountMapper;
    @Autowired
    JwtUtil jwtUtil;

    @Override
    public String login(LoginUserInfo loginUserInfo) {
        String username = loginUserInfo.getUsername();
        String password = loginUserInfo.getPassword();

        UserAccount check = userAccountMapper.selectByUsername(username);

        if(check == null) throw new UnauthorizedException(401,"用户不存在");

        boolean ok = BCrypt.checkpw(password,check.getPasswordHash());
   /*     boolean ok;
        if(password.equals(check.getPasswordHash())) ok = true;
        else ok = false;*/

        if(!ok) throw new UnauthorizedException(401,"密码错误");
        else{
            String result = jwtUtil.generateToken(check.getId(),check.getUsername());
            return result;
        }
    }

    @Override
    public UserInfo register(RegisterUserInfo info) {
        if(userAccountMapper.selectByUsername(info.getUsername()) != null)
            throw new UnauthorizedException(409,"用户名已存在");

        UserAccount newAccount = new UserAccount();
        newAccount.setUsername(info.getUsername());
        newAccount.setPasswordHash(BCrypt.hashpw(info.getPassword(),BCrypt.gensalt()));

        userAccountMapper.insert(newAccount);

        //UserAccount userAccount = userAccountMapper.selectByUsername(info.getUsername());

        UserInfo result = new UserInfo(newAccount.getId(),newAccount.getUsername());

        return result;
    }
}
