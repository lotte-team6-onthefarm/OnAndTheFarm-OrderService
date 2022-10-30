package com.team6.onandthefarmorderservice;


import com.team6.onandthefarmorderservice.dto.OrderDto;
import com.team6.onandthefarmorderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
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
    }

    /**
     * orderDto 안에는
     * adjustmentType : 해당 body의 type
     * productIdList : orderProductList(주문서 내에 담긴 상품들의 정보(productId, qty)
     * @param orderDto : 주문에 대한 정보가 담긴 DTO
     * @return
     */
    private ParticipantLink tryOrder(OrderDto orderDto) {
        final String requestURL = "http://"+env.getProperty("serviceurl.product")+"/api/user/product/product-service/order-try";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("adjustmentType", "ORDER");
        requestBody.put("productIdList", orderDto.getProductList());
        requestBody.put("orderSerial",orderDto.getOrderSerial());
        return tccRestAdapter.doTry(requestURL, requestBody);
    }

    private ParticipantLink tryPayment(OrderDto orderDto) {
        final String requestURL = "http://"+env.getProperty("serviceurl.payment")+"/api/user/payment/payment-service/payment-try";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("adjustmentType", "ORDER");
        requestBody.put("productIdList", orderDto.getProductList());
        requestBody.put("orderSerial",orderDto.getOrderSerial());
        return tccRestAdapter.doTry(requestURL, requestBody);
    }
}
