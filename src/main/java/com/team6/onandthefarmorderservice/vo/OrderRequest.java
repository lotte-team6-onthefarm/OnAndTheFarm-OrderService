package com.team6.onandthefarmorderservice.vo;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 *  주문 생성을 위해 사용되는 객체
 */
public class OrderRequest {
    private List<OrderProductRequest> productList;
    private Long userId;
    private String orderRecipientName;
    private String orderAddress;
    private String orderPhone;
    private String orderRequest;
    private Long feedNumber;
}
