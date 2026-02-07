package com.utgaming.testconcurrency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.utgaming.testconcurrency.entity.login.UserAccount;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


public interface UserAccountMapper extends BaseMapper<UserAccount> {
    @Select("select * from user_account where username = #{username} limit 1")
    UserAccount selectByUsername(@Param("username") String username);
}
