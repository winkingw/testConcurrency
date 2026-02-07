package com.utgaming.testconcurrency.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CacheData {
    private Object data;
    private LocalDateTime expireTime;
}
