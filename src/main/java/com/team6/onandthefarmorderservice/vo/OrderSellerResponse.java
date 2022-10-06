package com.team6.onandthefarmorderservice.vo;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * 셀러가 주문내역 조회를 하기 위해 사용하는 응답 객체
 */
public class OrderSellerResponse {
    private Long orderProductId;

    private String userName;

    private String ordersDate;

    private String ordersSerial;

    private Integer orderProductQty;

    private String orderProductName;

    private Integer orderProductPrice;

    private String orderProductMainImg;

    private String orderProductStatus;

    private String orderProductDeliveryWaybillNumber;

    private String orderProductDeliveryCompany;

    private String orderProductDeliveryDate;

    private Integer orderTotalPrice;
}
