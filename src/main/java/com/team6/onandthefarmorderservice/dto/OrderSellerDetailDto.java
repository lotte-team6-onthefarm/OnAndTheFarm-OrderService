package com.team6.onandthefarmorderservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSellerDetailDto {
    private String orderSerial;

    private String sellerId;
}
