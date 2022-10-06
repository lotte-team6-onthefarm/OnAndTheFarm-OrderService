package com.team6.onandthefarmorderservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderProductDto {
    private Long productId;
    private Long sellerId;
    private String productName;
    private String productImg;
    private Integer productPrice;
    private Integer productQty;
}
