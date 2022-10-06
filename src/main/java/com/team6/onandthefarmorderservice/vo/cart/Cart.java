package com.team6.onandthefarmorderservice.vo.cart;

import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    private Long cartId;

    private Long productId;

    private Long userId;

    private Integer cartQty;

    private Boolean cartIsActivated;

    private Boolean cartStatus;

    private String cartCreatedAt;
}
