package com.team6.onandthefarmorderservice;


import com.team6.onandthefarmorderservice.dto.OrderDto;
import com.team6.onandthefarmorderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TCC {

    private final TccRestAdapter tccRestAdapter;

    private final OrderService orderService;

    private final Environment env;

    public void placeOrder(OrderDto orderDto) {
        List<ParticipantLink> participantLinks = new ArrayList<>();

        // 재고 차감(Try)
        participantLinks.add(tryOrder(orderDto));

        // 결제 시도(try)
        participantLinks.add(tryPayment(orderDto));

        log.info("place 단 orderDto : "+orderDto.getProductList().toString());
        try{
            // 주문 생성
            Boolean result = orderService.createOrder(orderDto);
            if(!result) throw new RuntimeException();
        }catch (RuntimeException e){
            e.printStackTrace();
            tccRestAdapter.cancelAll(participantLinks);
            throw new RuntimeException();
        }


        // 재고 차감 확정(Confirm)
        tccRestAdapter.stockConfirm(participantLinks.get(0).getUri());

        // 결제 확정(Confirm)
        tccRestAdapter.paymentConfirm(participantLinks.get(1).getUri());
    }

    /**
     * 넛지 포인트를 위한 메서드
     * @param orderDto
     */
    public void placePointOrder(OrderDto orderDto,Long feedNumber) {
        List<ParticipantLink> participantLinks = new ArrayList<>();

        // 재고 차감(Try)
        participantLinks.add(tryOrder(orderDto));

        // 결제 시도(try)
        participantLinks.add(tryPayment(orderDto));

        // 포인트 시도(try)
        participantLinks.add(tryPoint(orderDto,feedNumber));

        try{
            // 주문 생성
            orderService.createOrder(orderDto);
        }catch (RuntimeException e){
            e.printStackTrace();
            tccRestAdapter.cancelAll(participantLinks);
            throw new RuntimeException();
        }


        // 재고 차감 확정(Confirm)
        tccRestAdapter.stockConfirm(participantLinks.get(0).getUri());

        // 결제 확정(Confirm)
        tccRestAdapter.paymentConfirm(participantLinks.get(1).getUri());

        // 포인트 확정(Confirm)
        tccRestAdapter.pointConfirm(participantLinks.get(2).getUri());
    }

    /**
     * orderDto 안에는
     * adjustmentType : 해당 body의 type
     * productIdList : orderProductList(주문서 내에 담긴 상품들의 정보(productId, qty)
     * @param orderDto : 주문에 대한 정보가 담긴 DTO
     * @return
     */
    @Transactional
    ParticipantLink tryOrder(OrderDto orderDto) {
        final String requestURL = "http://"+env.getProperty("serviceurl.product")+"/api/feign/user/product/product-service/order-try";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("adjustmentType", "ORDER");
        requestBody.put("productIdList", orderDto.getProductList());
        requestBody.put("orderSerial",orderDto.getOrderSerial());
        return tccRestAdapter.doTry(requestURL, requestBody);
    }

    @Transactional
    ParticipantLink tryPayment(OrderDto orderDto) {
        final String requestURL = "http://"+env.getProperty("serviceurl.payment")+"/api/feign/user/payment/payment-service/payment-try";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("adjustmentType", "ORDER");
        requestBody.put("productIdList", orderDto.getProductList());
        requestBody.put("orderSerial",orderDto.getOrderSerial());
        requestBody.put("imp_uid",orderDto.getImp_uid());
        requestBody.put("merchant_uid",orderDto.getMerchant_uid());
        requestBody.put("paid_amount",orderDto.getPaid_amount());
        return tccRestAdapter.doTry(requestURL, requestBody);
    }

    @Transactional
    ParticipantLink tryPoint(OrderDto orderDto,Long feedNumber) {
        final String requestURL = "http://"+env.getProperty("serviceurl.point")+"/api/feign/user/members/member-service/member-try";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("adjustmentType", "ORDER");
        requestBody.put("feedNumber", String.valueOf(feedNumber)); // feed id 받음
        requestBody.put("orderSerial",orderDto.getOrderSerial());
        return tccRestAdapter.doTry(requestURL, requestBody);
    }
}
