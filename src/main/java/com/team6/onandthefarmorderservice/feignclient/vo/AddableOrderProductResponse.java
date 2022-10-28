package com.team6.onandthefarmorderservice.feignclient.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddableOrderProductResponse {

    private Long orderProductId;

    private Long productId;

    private Long sellerId;
}
