package com.team6.onandthefarmorderservice.entity;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

@Builder
@Slf4j
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SequenceGenerator(
        name="REFUND_SEQ_GENERATOR",
        sequenceName = "REFUND_SEQ",
        initialValue = 100000, allocationSize = 1
)
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "REFUND_SEQ_GENERATOR")
    private Long refundId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordersId")
    private Orders orders;

    private String refundContent;

    private String refundImage;

    private Long orderProductId;

    private Long userId;
}
