package com.utgaming.testconcurrency.service;

import com.utgaming.testconcurrency.controller.dto.ProductCreateRequest;
import com.utgaming.testconcurrency.controller.dto.ProductUpdateRequest;
import com.utgaming.testconcurrency.entity.Product;
import jakarta.validation.Valid;

public interface ProductService {
    Product getProductById(Long id);

    Product createProduct(ProductCreateRequest request);

    Product updateProduct(ProductUpdateRequest request, Long id);

    Product deleteProduct(Long id);
}
