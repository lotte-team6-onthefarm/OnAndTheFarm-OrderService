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
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long paymentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordersId")
    private Orders orders;

    private String paymentDate;

    private String paymentMethod;

    private Boolean paymentStatus;

    private Integer paymentDepositAmount;

    private String paymentDepositName;

    private String paymentDepositBank;

    private String paymentRefundAccount;

    private String paymentRefundAccountName;
}
