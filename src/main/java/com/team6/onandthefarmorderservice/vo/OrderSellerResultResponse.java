package com.team6.onandthefarmorderservice.vo;

import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSellerResultResponse {
    List<OrderSellerResponse> responses;

    private Integer currentPageNum;

    private Integer totalPageNum;
}
