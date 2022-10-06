package com.team6.onandthefarmorderservice.vo;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    private Long userId;

    private Long orderProductId;

    private String refundDetail;

    private String refundImage;
}
