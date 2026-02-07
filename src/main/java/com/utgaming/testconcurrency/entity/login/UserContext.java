package com.utgaming.testconcurrency.entity.login;

public class UserContext {
    private static final ThreadLocal<UserInfo> USER = new ThreadLocal<>();

    private UserContext() {}

    public static void set(UserInfo info) {
        USER.set(info);
    }

    public static UserInfo get() {
        return USER.get();
    }

    public static void clear() {
        USER.remove();
    }
}
