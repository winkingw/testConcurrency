package com.utgaming.testconcurrency.handle;

import com.utgaming.testconcurrency.common.UnauthorizedException;
import com.utgaming.testconcurrency.entity.login.UserContext;
import com.utgaming.testconcurrency.entity.login.UserInfo;
import com.utgaming.testconcurrency.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;

    public JwtAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new UnauthorizedException(401,"丢失或不合法header");
        }

        String token = auth.substring(7).trim();
        if (token.isEmpty()){
            throw new UnauthorizedException(401,"丢失token");
        }

        Claims claims = jwtUtil.parseToken(token);

        Long userId = Long.valueOf(claims.getSubject());
        String username = claims.get("username", String.class);

        //request.setAttribute("user", new UserInfo(userId, username));

        UserContext.set(new UserInfo(userId,username));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        UserContext.clear();
    }
}
