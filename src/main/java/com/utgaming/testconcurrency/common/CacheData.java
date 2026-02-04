package com.utgaming.testconcurrency.common;

import java.time.LocalDateTime;

public class CacheData {
    private Object data;
    private LocalDateTime expireTime;

    public Object getData() {return data;}
    public void setData(Object data) {this.data = data;}

    public LocalDateTime getExpireTime() {return expireTime;}
    public void setExpireTime(LocalDateTime expireTime) {this.expireTime = expireTime;}
}
