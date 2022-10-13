package com.team6.onandthefarmorderservice.feignclient.service;

import com.team6.onandthefarmorderservice.feignclient.vo.OrderClientOrderProductIdResponse;

public interface ProductServiceClientService {
	OrderClientOrderProductIdResponse getProductIdByOrderProductId(Long orderProductId);
}
