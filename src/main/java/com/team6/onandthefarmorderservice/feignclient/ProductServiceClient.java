package com.team6.onandthefarmorderservice.feignclient;

import com.team6.onandthefarmorderservice.vo.product.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", contextId = "product-service-product")
public interface ProductServiceClient {
    /**
     * 제품ID를 이용해 제품의 정보를 가져오는 것
     * @param productId
     * @return
     */
    @GetMapping("/api/user/product-service/product/{product-no}")
    public Product findByProductId(@PathVariable("product-no") Long productId);
}
