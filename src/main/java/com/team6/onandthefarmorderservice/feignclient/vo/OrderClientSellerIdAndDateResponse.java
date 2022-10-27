package com.team6.onandthefarmorderservice.feignclient.vo;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderClientSellerIdAndDateResponse {

    private Long ordersId;

    private Integer orderProductPrice;

    private Integer orderProductQty;
}
