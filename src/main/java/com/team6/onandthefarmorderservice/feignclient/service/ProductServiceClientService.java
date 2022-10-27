package com.team6.onandthefarmorderservice.feignclient.service;

import com.team6.onandthefarmorderservice.feignclient.vo.OrderClientOrderProductIdResponse;
import com.team6.onandthefarmorderservice.feignclient.vo.OrderClientSellerIdAndDateResponse;
import com.team6.onandthefarmorderservice.feignclient.vo.OrdersByUserResponse;

import java.util.List;

public interface ProductServiceClientService {
	OrderClientOrderProductIdResponse getProductIdByOrderProductId(Long orderProductId);

	List<OrdersByUserResponse> getOrdersByUserId(Long userId);

	List<OrderClientOrderProductIdResponse> getOrderProductByOrdersId(Long ordersId);

	List<OrderClientSellerIdAndDateResponse> findBySellerIdAndOrderProductDateStartingWith(Long sellerId, String nextDate);
}
