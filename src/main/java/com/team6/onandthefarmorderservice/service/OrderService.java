package com.team6.onandthefarmorderservice.service;


import com.team6.onandthefarmorderservice.dto.*;
import com.team6.onandthefarmorderservice.vo.*;

import java.util.List;

public interface OrderService {
    OrderSheetResponse findOneByProductId(OrderSheetDto orderSheetDto);
    List<OrderFindOneResponse> findCartByUserId(Long userId);
    Boolean createOrder(OrderDto orderDto);
    List<OrderSellerResponseList> findSellerOrders(OrderSellerFindDto orderSellerFindDto);
    List<OrderUserResponseList> findUserOrders(OrderUserFindDto orderUserFindDto);
    OrderUserDetailResponse findUserOrderDetail(String orderSerial);
    OrderUserDetailResponse findSellerOrderDetail(OrderSellerDetailDto orderSellerDetailDto);
    void createPayment(String orderSerial);
    List<OrderSellerResponse> findSellerClaims(OrderSellerRequest orderSellerRequest);
    List<OrderSellerResponse> findUserClaims(OrderUserFindDto orderUserFindDto);
    Boolean createCancel(RefundDto refundDto);
    Boolean createRefund(RefundDto refundDto);
    RefundDetailResponse findRefundDetail(Long orderProductId);
    Boolean conformRefund(Long orderProductId);
    Boolean deliveryStart(OrderDeliveryDto orderDeliveryDto);
    Boolean deliveryConform(String orderSerial);
}
