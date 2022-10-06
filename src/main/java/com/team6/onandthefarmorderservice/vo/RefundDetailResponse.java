package com.team6.onandthefarmorderservice.vo;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundDetailResponse {
    private String productName;

    private Integer productPrice;

    private String cancelDetail;

    private Integer productQty;

    private String productStatus;

    private String refundImage;

    private String productImage;

    private String userName;

    private String userAddress;

    private String userPhone;

    private Integer productTotalPrice;
}
