package com.team6.onandthefarmorderservice.feignclient.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team6.onandthefarmorderservice.feignclient.vo.OrderClientOrderProductIdResponse;
import com.team6.onandthefarmorderservice.repository.OrderProductRepository;

@Service
@Transactional
public class ProductServiceClientServiceImpl implements ProductServiceClientService {

	private OrderProductRepository orderProductRepository;

	@Autowired
	public ProductServiceClientServiceImpl(OrderProductRepository orderProductRepository) {
		this.orderProductRepository = orderProductRepository;
	}

	public OrderClientOrderProductIdResponse getProductIdByOrderProductId(Long orderProductId) {
		Long productId = orderProductRepository.findById(orderProductId).get().getProductId();

		OrderClientOrderProductIdResponse orderClientOrderProductIdResponse = OrderClientOrderProductIdResponse.builder()
				.orderProductId(orderProductId)
				.productId(productId)
				.build();

		return orderClientOrderProductIdResponse;
	}
}
