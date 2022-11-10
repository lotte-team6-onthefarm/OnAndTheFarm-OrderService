package com.team6.onandthefarmorderservice.vo.feignclient;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVo {
    private String imp_uid;

    private String merchant_uid;

    private String  paid_amount;
}
