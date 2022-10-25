package com.team6.onandthefarmorderservice.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrdersConditionResponse {

    private Integer beforeDelivery;
    private Integer requestRefund;
    private Integer cancelOrders;
    private Integer delivering;
    private Integer notSelling;
    private Integer beforeAnswer;
    private Integer sellingProducts;
    private Integer deliverCompletes;
}
