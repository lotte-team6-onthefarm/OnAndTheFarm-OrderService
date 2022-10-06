package com.team6.onandthefarmorderservice.vo;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSellerDetailRequest {
    private String orderSerial;

    private String sellerId;
}
