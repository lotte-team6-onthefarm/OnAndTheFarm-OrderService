package com.team6.onandthefarmorderservice.feignclient.service;

import com.team6.onandthefarmorderservice.entity.Orders;
import com.team6.onandthefarmorderservice.feignclient.vo.OrderVo;
import com.team6.onandthefarmorderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceClientImpl implements PaymentServiceClient{
    private final OrderRepository orderRepository;

    @Override
    public OrderVo findByOrderserial(String orderSerial) {
        Orders orders = orderRepository.findByOrdersSerial(orderSerial);

        OrderVo orderVo = OrderVo.builder()
                .userId(orders.getUserId())
                .build();
        return orderVo;
    }
}
