package com.team6.onandthefarmorderservice.feignclient.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderClientOrderProductIdResponse {
	private Long orderProductId;
	private Long productId;
	private String ordersDate;
}
