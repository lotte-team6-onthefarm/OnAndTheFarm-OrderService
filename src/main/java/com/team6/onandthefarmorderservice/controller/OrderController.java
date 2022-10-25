package com.team6.onandthefarmorderservice.controller;

import com.team6.onandthefarmorderservice.TCC;
import com.team6.onandthefarmorderservice.dto.OrderDto;
import com.team6.onandthefarmorderservice.dto.OrderProductDto;
import com.team6.onandthefarmorderservice.service.OrderService;
import com.team6.onandthefarmorderservice.vo.OrderProductRequest;
import com.team6.onandthefarmorderservice.vo.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private final TCC tcc;

    @PostMapping("/api/user/orders")
    public ResponseEntity createOrder(
            @RequestBody OrderRequest orderRequest){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        OrderDto orderDto = OrderDto.builder()
                .orderRecipientName(orderRequest.getOrderRecipientName())
                .orderRequest(orderRequest.getOrderRequest())
                .orderPhone(orderRequest.getOrderPhone())
                .orderAddress(orderRequest.getOrderAddress())
                .userId(1l)
                .orderSerial(UUID.randomUUID().toString())
                .productList(new ArrayList<>())
                .build();

        for(OrderProductRequest order : orderRequest.getProductList()){
            OrderProductDto orderProductDto = OrderProductDto.builder()
                    .productQty(order.getProductQty())
                    .productId(order.getProductId())
                    .productPrice(1000)
                    .build();
            orderDto.getProductList().add(orderProductDto);
        }

        try{
            tcc.placeOrder(orderDto);
        } catch (RuntimeException e){
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        //orderService.createOrder(orderDto);

        return new ResponseEntity(HttpStatus.OK);
    }
}
