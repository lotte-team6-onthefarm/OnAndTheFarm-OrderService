package com.team6.onandthefarmorderservice.feignclient.controller;

import com.team6.onandthefarmorderservice.feignclient.service.PaymentServiceClient;
import com.team6.onandthefarmorderservice.feignclient.vo.OrderVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceClientController {

    private final PaymentServiceClient paymentServiceClient;

    @GetMapping("/api/feign/user/orders/order-service/{order-serial}")
    public OrderVo findByOrderSerial(@PathVariable("order-serial") String orderSerial){
        return paymentServiceClient.findByOrderserial(orderSerial);
    }
}
