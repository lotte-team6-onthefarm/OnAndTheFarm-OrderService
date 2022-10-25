package com.team6.onandthefarmorderservice.vo;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * 주문 내역 조회를 위해 사용되는 요청 객체
 */
public class OrderSellerRequest {
    private String sellerId;

    private String startDate;

    private String endDate;

    private Integer pageNumber;

    private String ordersStatus;
}
