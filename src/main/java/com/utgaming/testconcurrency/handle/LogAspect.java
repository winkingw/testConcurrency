package com.utgaming.testconcurrency.handle;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LogAspect {
    @Around("execution(* com.utgaming.testconcurrency.controller..*(..))")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = pjp.getSignature().toShortString();

        try {
            Object result = pjp.proceed();
            log.info("API ok:{} cost = {}ms", methodName, System.currentTimeMillis() - start);
            return result;
        }catch (Throwable e){
            log.error("API error:{} cost = {}ms", methodName, System.currentTimeMillis() - start, e);
            throw e;
        }
    }
}
