package com.team6.onandthefarmorderservice.vo;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * 주문서 조회를 위한 객체
 */
public class OrderFindOneResponse {
    private Long productId;

    private Long sellerId;

    private String productName;

    private String productImg;

    private Integer productPrice;

    private Integer productQty;
}
