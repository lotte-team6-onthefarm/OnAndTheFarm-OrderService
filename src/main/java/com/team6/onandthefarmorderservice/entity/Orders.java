package com.team6.onandthefarmorderservice.entity;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

@Builder
@Slf4j
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    private String ordersDeliveryStatus;

    private String ordersDeliveryWaybillNumber;

    private String ordersDeliveryCompany;

    private String ordersDeliveryDate;

    private String ordersSerial;
}
