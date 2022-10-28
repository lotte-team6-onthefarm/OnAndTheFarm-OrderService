package com.team6.onandthefarmorderservice.feignclient.controller;

import com.team6.onandthefarmorderservice.feignclient.service.SnsServiceClientService;
import com.team6.onandthefarmorderservice.feignclient.vo.AddableOrderProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SnsServiceClientController {

    private final SnsServiceClientService snsServiceClientService;

    @Autowired
    public SnsServiceClientController(SnsServiceClientService snsServiceClientService){
        this.snsServiceClientService = snsServiceClientService;
    }

    @GetMapping("/api/user/order/order-service/list/add/{user-no}")
    List<AddableOrderProductResponse> findAddableOrderProductList(@PathVariable("user-no") Long memberId){
        return snsServiceClientService.findAddableOrderProductList(memberId);
    }
}
