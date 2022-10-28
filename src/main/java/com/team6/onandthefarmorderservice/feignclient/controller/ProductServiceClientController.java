package com.team6.onandthefarmorderservice.feignclient.controller;

import com.team6.onandthefarmorderservice.feignclient.vo.OrderClientSellerIdAndDateResponse;
import com.team6.onandthefarmorderservice.feignclient.vo.OrdersByUserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team6.onandthefarmorderservice.feignclient.service.ProductServiceClientService;
import com.team6.onandthefarmorderservice.feignclient.vo.OrderClientOrderProductIdResponse;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@Slf4j
public class ProductServiceClientController {

	ProductServiceClientService productServiceClientService;

	@Autowired
	public ProductServiceClientController(ProductServiceClientService productServiceClientService) {
		this.productServiceClientService = productServiceClientService;
	}

	@GetMapping("/api/user/orders/order-product/{order-product-no}")
	OrderClientOrderProductIdResponse findProductIdByOrderProductId(
			@PathVariable("order-product-no") Long orderProductId) {
		return productServiceClientService.getProductIdByOrderProductId(orderProductId);
	}

	@GetMapping("/api/user/orders/order-service/review-available/{user-no}")
	List<OrdersByUserResponse> findProductWithoutReview(@PathVariable("user-no") Long userId){
		return productServiceClientService.getOrdersByUserId(userId);
	}

	@GetMapping("/api/user/orders/order-service/order-product/{orders-no}")
	List<OrderClientOrderProductIdResponse> findByOrdersId(@PathVariable("orders-no") Long ordersId){
		return productServiceClientService.getOrderProductByOrdersId(ordersId);
	}

	@GetMapping("/api/user/orders/order-service/order-product/orders-list")
	List<OrderClientSellerIdAndDateResponse> findBySellerIdAndOrderProductDateStartingWith(@RequestParam Long sellerId, @RequestParam String nextDate){
		return productServiceClientService.findBySellerIdAndOrderProductDateStartingWith(sellerId, nextDate);
	}
}
