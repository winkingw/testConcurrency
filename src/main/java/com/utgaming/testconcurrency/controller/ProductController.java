package com.utgaming.testconcurrency.controller;


import com.utgaming.testconcurrency.common.Result;
import com.utgaming.testconcurrency.controller.dto.ProductCreateRequest;
import com.utgaming.testconcurrency.controller.dto.ProductUpdateRequest;
import com.utgaming.testconcurrency.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
@Validated
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

    @PostMapping("/{id}/deduct")
    public Result deductProduct(@PathVariable @NotNull @Min(1) Long id
                                ,@RequestParam @NotNull @Min(1) Integer count) {
        return Result.success(productService.deductStock(id,count));
    }

    @PostMapping("/{id}/preheat")
    public Result preheatProduct(@PathVariable @NotNull @Min(1) Long id) {
        return Result.success(productService.preheatStock(id));
    }
}
