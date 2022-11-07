package com.team6.onandthefarmorderservice.vo;

import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointOrderRequest {
    private List<OrderProductRequest> productList;
    private Long userId;
    private String orderRecipientName;
    private String orderAddress;
    private String orderPhone;
    private String orderRequest;
    private Long pointMemberId;
}
