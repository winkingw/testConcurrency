package com.utgaming.testconcurrency.handle;

import com.utgaming.testconcurrency.common.BusinessException;
import com.utgaming.testconcurrency.common.Result;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result handleBusiness(BusinessException ex) {
        return Result.error(ex.getCode(),ex.getMessage(),null);
    }

    @ExceptionHandler(Exception.class)
    public Result handleOther(Exception ex) {
        return Result.error(500,"系统错误",null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result handleConstraint(ConstraintViolationException ex) {
        return Result.error(400,ex.getMessage(),null);
    }
}
