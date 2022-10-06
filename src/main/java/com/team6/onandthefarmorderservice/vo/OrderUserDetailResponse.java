package com.team6.onandthefarmorderservice.vo;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderUserDetailResponse {
    List<OrderFindOneDetailResponse> orderProducts;

    private String orderName;

    private String orderPhone;

    private String orderAddress;

    private String orderRequest;

    private String orderDate;

    private String orderStatus;

    private Integer orderTotalPrice;
}
