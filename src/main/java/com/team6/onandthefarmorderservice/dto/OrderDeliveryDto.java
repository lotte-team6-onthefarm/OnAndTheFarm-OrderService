package com.team6.onandthefarmorderservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDeliveryDto {
    private String orderSerial;

    private String orderDeliveryCompany;

    private String orderDeliveryWaybillNumber;
}
