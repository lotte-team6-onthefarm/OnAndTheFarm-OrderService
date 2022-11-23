package com.team6.onandthefarmorderservice.feignclient.service;

import com.team6.onandthefarmorderservice.feignclient.vo.OrderVo;

public interface PaymentServiceClient {
    OrderVo findByOrderserial(String orderSerial);
}
