package com.team6.onandthefarmorderservice.feignclient.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderVo {
    private Long ordersId;

    private Long userId;

    private String ordersDate;

    private String ordersStatus;

    private Integer ordersTotalPrice;

    private String ordersRecipientName;

    private String ordersAddress;

    private String ordersPhone;

    private String ordersRequest;

    private Long ordersSellerId;

    //private String ordersDeliveryStatus;

    private String ordersDeliveryWaybillNumber;

    private String ordersDeliveryCompany;

    private String ordersDeliveryDate;

    private String ordersSerial;
}
