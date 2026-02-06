package com.utgaming.testconcurrency.common;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StockDeductMessage {
    long productId;

    int count;

    String requestId;

    LocalDateTime timestamp;
}
