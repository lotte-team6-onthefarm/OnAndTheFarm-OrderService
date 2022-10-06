package com.team6.onandthefarmorderservice.vo;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderFindOneDetailResponse {
    private Long orderProductId;

    private String productName;

    private String productImg;

    private Integer productPrice;

    private Integer productQty;
}
