package com.team6.onandthefarmorderservice.vo.feignclient;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductQnaVo {
    private Long productQnaId;

    private Long productId;

    private Long userId;

    private Long sellerId;

    private String productQnaContent;

    private String productQnaCreatedAt;

    private String productQnaModifiedAt;

    private String productQnaStatus;
}
