package com.team6.onandthefarmorderservice.feignclient;


import com.team6.onandthefarmorderservice.vo.feignclient.ProductQnaVo;
import com.team6.onandthefarmorderservice.vo.feignclient.ProductVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "product-service", contextId = "product-service-product")
public interface ProductServiceClient {
    /**
     * 제품ID를 이용해 제품의 정보를 가져오는 것
     * @param productId
     * @return
     */
    @GetMapping("/api/user/product/product-service/{product-no}")
    ProductVo findByProductId(@PathVariable("product-no") Long productId);
    @GetMapping("/api/user/product/product-service/no-selling/{seller-no}")
    List<ProductVo> findNotSellingProduct(@PathVariable("seller-no") Long sellerId);
    @GetMapping("/api/user/product/product-service/selling/{seller-no}")
    List<ProductVo> findSellingProduct(@PathVariable("seller-no") Long sellerId);

    @GetMapping("/api/user/product/product-service/qna/{seller-no}")
    List<ProductQnaVo> findBeforeAnswerProductQna(@PathVariable("seller-no") Long sellerId);


}
