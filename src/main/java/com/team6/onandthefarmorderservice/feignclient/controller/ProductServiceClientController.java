package com.team6.onandthefarmorderservice.feignclient.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.team6.onandthefarmorderservice.feignclient.service.ProductServiceClientService;
import com.team6.onandthefarmorderservice.feignclient.vo.OrderClientOrderProductIdResponse;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class ProductServiceClientController {

	ProductServiceClientService productServiceClientService;

	public ProductServiceClientController(ProductServiceClientService productServiceClientService) {
		this.productServiceClientService = productServiceClientService;
	}

	@GetMapping("/api/user/orders/order-product/{order-product-no}")
	OrderClientOrderProductIdResponse findProductIdByOrderProductId(
			@PathVariable("order-product-no") Long orderProductId) {
		return productServiceClientService.getProductIdByOrderProductId(orderProductId);
	}
}
