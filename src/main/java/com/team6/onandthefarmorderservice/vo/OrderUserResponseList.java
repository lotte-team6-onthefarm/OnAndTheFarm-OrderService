package com.team6.onandthefarmorderservice.vo;

import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderUserResponseList {
    List<OrderUserResponse> orderUserResponses;

    private Integer orderTotalPrice;

    private String orderDate;

    private String orderProductDeliveryWaybillNumber;

    private String orderProductDeliveryCompany;

    private String orderProductDeliveryDate;

    private String ordersSerial;

    private String orderStatus;
}
