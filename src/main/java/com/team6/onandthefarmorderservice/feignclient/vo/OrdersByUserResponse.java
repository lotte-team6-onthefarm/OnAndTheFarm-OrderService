package com.team6.onandthefarmorderservice.feignclient.vo;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdersByUserResponse {

    private Long ordersId;
    private Long sellerId;
    private String ordersDate;

}
