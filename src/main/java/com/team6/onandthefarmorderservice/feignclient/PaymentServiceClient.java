package com.team6.onandthefarmorderservice.feignclient;

import com.team6.onandthefarmorderservice.vo.feignclient.PaymentVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentServiceClient {
    @PostMapping("/api/feign/payment-service/cancel")
    Boolean cancelPayment(@RequestBody PaymentVo paymentVo);
}
