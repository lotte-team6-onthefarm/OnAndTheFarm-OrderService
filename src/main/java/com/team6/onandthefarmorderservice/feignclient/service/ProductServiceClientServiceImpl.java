package com.team6.onandthefarmorderservice.feignclient.service;

import com.team6.onandthefarmorderservice.entity.OrderProduct;
import com.team6.onandthefarmorderservice.entity.Orders;
import com.team6.onandthefarmorderservice.feignclient.vo.OrderClientSellerIdAndDateResponse;
import com.team6.onandthefarmorderservice.feignclient.vo.OrdersByUserResponse;
import com.team6.onandthefarmorderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team6.onandthefarmorderservice.feignclient.vo.OrderClientOrderProductIdResponse;
import com.team6.onandthefarmorderservice.repository.OrderProductRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProductServiceClientServiceImpl implements ProductServiceClientService {

	private OrderProductRepository orderProductRepository;
	private OrderRepository orderRepository;

	@Autowired
	public ProductServiceClientServiceImpl(OrderProductRepository orderProductRepository,
										   OrderRepository orderRepository) {
		this.orderProductRepository = orderProductRepository;
		this.orderRepository = orderRepository;
	}

	public OrderClientOrderProductIdResponse getProductIdByOrderProductId(Long orderProductId) {
		Long productId = orderProductRepository.findById(orderProductId).get().getProductId();

		OrderClientOrderProductIdResponse orderClientOrderProductIdResponse = OrderClientOrderProductIdResponse.builder()
				.orderProductId(orderProductId)
				.productId(productId)
				.ordersDate(orderProductRepository.findById(orderProductId).get().getOrders().getOrdersDate())
				.build();

		return orderClientOrderProductIdResponse;
	}

	@Override
	public List<OrdersByUserResponse> getOrdersByUserId(Long userId) {

		List<OrdersByUserResponse> ordersByUserResponseList = new ArrayList<>();

		List<Orders> ordersList = orderRepository.findWithOrderAndOrdersStatus(userId);
		for(Orders orders : ordersList){
			OrdersByUserResponse ordersByUserResponse = OrdersByUserResponse.builder()
					.ordersId(orders.getOrdersId())
					.sellerId(orders.getOrdersSellerId())
					.ordersDate(orders.getOrdersDate())
					.build();

			ordersByUserResponseList.add(ordersByUserResponse);
		}

		return ordersByUserResponseList;
	}

	@Override
	public List<OrderClientOrderProductIdResponse> getOrderProductByOrdersId(Long ordersId) {

		List<OrderClientOrderProductIdResponse> orderProductByIdList = new ArrayList<>();

		List<OrderProduct> orderProductList = orderProductRepository.findByOrdersAndStatus(ordersId);
		for(OrderProduct orderProduct : orderProductList){
			OrderClientOrderProductIdResponse response = OrderClientOrderProductIdResponse.builder()
					.orderProductId(orderProduct.getOrderProductId())
					.productId(orderProduct.getProductId())
					.build();

			orderProductByIdList.add(response);
		}

		return orderProductByIdList;
	}

	@Override
	public List<OrderClientSellerIdAndDateResponse> findBySellerIdAndOrderProductDateStartingWith(Long sellerId, String nextDate) {

		List<OrderClientSellerIdAndDateResponse> orderProductResponseList = new ArrayList<>();

		List<OrderProduct> orderProductList = orderProductRepository.findBySellerIdAndOrderProductDateStartingWith(sellerId, nextDate);
		for(OrderProduct orderProduct : orderProductList){
			OrderClientSellerIdAndDateResponse response = OrderClientSellerIdAndDateResponse.builder()
					.ordersId(orderProduct.getOrders().getOrdersId())
					.orderProductPrice(orderProduct.getOrderProductPrice())
					.orderProductQty(orderProduct.getOrderProductQty())
					.build();

			orderProductResponseList.add(response);
		}

		return orderProductResponseList;
	}
}
