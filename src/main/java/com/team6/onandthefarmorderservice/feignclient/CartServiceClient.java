package com.team6.onandthefarmorderservice.feignclient;


import com.team6.onandthefarmorderservice.vo.feignclient.CartVo;
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
    @GetMapping("/api/user/cart/product-service/{user-no}")
    List<CartVo> findByUserId(@PathVariable("user-no") Long userId);
}
