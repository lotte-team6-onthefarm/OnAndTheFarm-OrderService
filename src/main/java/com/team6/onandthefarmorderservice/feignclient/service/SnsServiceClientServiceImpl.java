package com.team6.onandthefarmorderservice.feignclient.service;

import com.team6.onandthefarmorderservice.entity.OrderProduct;
import com.team6.onandthefarmorderservice.entity.Orders;
import com.team6.onandthefarmorderservice.feignclient.vo.AddableOrderProductResponse;
import com.team6.onandthefarmorderservice.repository.OrderProductRepository;
import com.team6.onandthefarmorderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class SnsServiceClientServiceImpl implements SnsServiceClientService{

    private final OrderRepository orderRepository;

    private final OrderProductRepository orderProductRepository;

    public SnsServiceClientServiceImpl(OrderRepository orderRepository,
                                       OrderProductRepository orderProductRepository){
        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
    }

    @Override
    public List<AddableOrderProductResponse> findAddableOrderProductList(Long memberId) {

        List<AddableOrderProductResponse> responseList = new ArrayList<>();

        List<Orders> ordersList = orderRepository.findWithOrderAndOrdersStatus(memberId);
        for(Orders orders : ordersList){
            List<OrderProduct> orderProductList = orderProductRepository.findByOrdersAndStatus(orders.getOrdersId());
            for(OrderProduct orderProduct : orderProductList){
                AddableOrderProductResponse productResponse = AddableOrderProductResponse.builder()
                        .orderProductId(orderProduct.getOrderProductId())
                        .productId(orderProduct.getProductId())
                        .sellerId(orderProduct.getSellerId())
                        .build();

                responseList.add(productResponse);
            }
        }

        return responseList;
    }
}
