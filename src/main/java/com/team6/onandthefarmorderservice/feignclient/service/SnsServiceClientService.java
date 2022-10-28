package com.team6.onandthefarmorderservice.feignclient.service;

import com.team6.onandthefarmorderservice.feignclient.vo.AddableOrderProductResponse;

import java.util.List;

public interface SnsServiceClientService {

    List<AddableOrderProductResponse> findAddableOrderProductList(Long memberId);
}
