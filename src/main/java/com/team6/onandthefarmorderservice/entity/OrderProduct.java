package com.team6.onandthefarmorderservice.entity;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

@Builder
@Slf4j
@Entity
@Table(name = "order_product")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SequenceGenerator(
        name="ORDER_PRODUCT_SEQ_GENERATOR",
        sequenceName = "ORDER_PRODUCT_SEQ",
        initialValue = 100000, allocationSize = 1
)
public class OrderProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "ORDER_PRODUCT_SEQ_GENERATOR")
    private Long orderProductId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordersId")
    private Orders orders;

    private Integer orderProductQty;

    private String orderProductName;

    private Long productId;

    private Integer orderProductPrice;

    private String orderProductMainImg;

    private Long sellerId;

    private String orderProductStatus;

    private String orderProductDate;

}
