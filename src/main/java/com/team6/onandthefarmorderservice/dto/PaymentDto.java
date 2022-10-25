package com.team6.onandthefarmorderservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private Long orderProductId;
    private OrderProductDto orderProductDto;
}
