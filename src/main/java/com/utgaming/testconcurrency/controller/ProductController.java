package com.utgaming.testconcurrency.controller;


import com.utgaming.testconcurrency.common.Result;
import com.utgaming.testconcurrency.controller.dto.ProductCreateRequest;
import com.utgaming.testconcurrency.controller.dto.ProductUpdateRequest;
import com.utgaming.testconcurrency.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@RestController
@RequestMapping("/product")
@Validated
@Slf4j
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public Result getProduct(@PathVariable @NotNull @Min(1) Long id) {
        return Result.success(productService.getProductById(id));
    }

    @PostMapping
    public Result addProduct(@RequestBody @Valid ProductCreateRequest req) {
        return Result.success(productService.createProduct(req));
    }

    @PatchMapping("/{id}")
    public Result updateProduct(@RequestBody @Valid ProductUpdateRequest req, @PathVariable @NotNull @Min(1) Long id) {
        return Result.success(productService.updateProduct(req, id));
    }

    @DeleteMapping("/{id}")
    public Result deleteProduct(@PathVariable @NotNull @Min(1) Long id) {
        return Result.success(productService.deleteProduct(id));
    }

    @RateLimiter(name = "deductLimiter",fallbackMethod = "rateLimitFallback")
    @CircuitBreaker(name = "deductBreaker", fallbackMethod = "circuitFallback")
    @PostMapping("/{id}/deduct")
    public Result deductProduct(@PathVariable @NotNull @Min(1) Long id
                                ,@RequestParam @NotNull @Min(1) Integer count) {
        return Result.success(productService.deductStock(id,count));
    }

    @PostMapping("/{id}/preheat")
    public Result preheatProduct(@PathVariable @NotNull @Min(1) Long id) {
        return Result.success(productService.preheatStock(id));
    }

    public Result rateLimitFallback(Long id, Integer count, Throwable t) {
        log.warn("限流", t);
        return Result.error(429, "限流触发", null);
    }
    public Result circuitFallback(Long id, Integer count, Throwable t) {
        log.warn("熔断", t);
        return Result.error(503, "熔断触发", null);
    }
}
