package com.utgaming.testconcurrency.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class IdCheckStrategyFactory {
    private final Map<String, IdCheckStrategy> strategies;

    @Value("${app.idcheck.strategy:bloom}")
    private String strategy;

    public IdCheckStrategyFactory(Map<String, IdCheckStrategy> strategies) {
        this.strategies = strategies;
    }

    public IdCheckStrategy getStrategy() {
        return strategies.get("bloomFilterStrategy");
    }
/*    public IdCheckStrategy getStrategy() {
        if("db".equalsIgnoreCase(strategy)) {
            return strategies.get("dbCheckStrategy");
        }
        return strategies.get("bloomFilterStrategy");
    }*/
}
