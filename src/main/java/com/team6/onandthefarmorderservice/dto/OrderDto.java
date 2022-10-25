package com.team6.onandthefarmorderservice.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long sellerId;

    private Long userId;

    private String orderRecipientName;

    private String orderAddress;

    private String orderPhone;

    private String orderRequest;

    private List<OrderProductDto> productList;

    private Map<Long,Long> prodSeller;

    private String orderSerial;
}
