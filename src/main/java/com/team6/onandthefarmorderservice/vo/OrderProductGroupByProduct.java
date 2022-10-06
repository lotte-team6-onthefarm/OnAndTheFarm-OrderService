package com.team6.onandthefarmorderservice.vo;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderProductGroupByProduct {
    private Integer orderProductQty;

    private String orderProductName;

    private Long productId;

    private Integer orderProductPrice;

    private String orderProductMainImg;

    private Long sellerId;

    private String orderProductStatus;

    private String orderProductDate;
}
