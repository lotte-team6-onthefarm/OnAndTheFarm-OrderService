package com.team6.onandthefarmorderservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundDto {
    private Long userId;

    private Long orderProductId;

    private String refundDetail;

    private String refundImage;
}
