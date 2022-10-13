package com.team6.onandthefarmorderservice.feignclient;

import com.team6.onandthefarmorderservice.vo.cart.Cart;
import com.team6.onandthefarmorderservice.vo.product.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "product-service", contextId = "product-service-cart")
public interface CartServiceClient {
    /**
     * 유저ID로 유저의 장바구니 목록을 가져오는 것
     * @param userId
     * @return
     */
    @GetMapping("/api/user/product-service/cart/{user-no}")
    public List<Cart> findByUserId(@PathVariable("user-no") Long userId);
}