package com.team6.onandthefarmorderservice.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team6.onandthefarmorderservice.TCC;
import com.team6.onandthefarmorderservice.dto.*;
import com.team6.onandthefarmorderservice.feignclient.PaymentServiceClient;
import com.team6.onandthefarmorderservice.service.OrderService;
import com.team6.onandthefarmorderservice.utils.BaseResponse;
import com.team6.onandthefarmorderservice.vo.*;
import com.team6.onandthefarmorderservice.vo.feignclient.PaymentVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.security.Principal;
import java.util.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/user/orders")
/** @Api(value = "주문",description = "주문 상태\n" +
        " * activated(os0) : 주문완료\n" +
        " * canceled(os1) : 주문취소\n" +
        " * refundRequest(os2) : 반품신청\n" +
        " * refundCompleted(os3) : 반품확정\n" +
        " * deliveryProgress(os4) : 배송 중\n" +
        " * deliveryCompleted(os5) : 배송 완료")
**/
public class UserOrderController {
    private final OrderService orderService;

    private final PaymentServiceClient paymentServiceClient;

    private final TCC tcc;

    //private final ProductOrderChannelAdapter productOrderChannelAdapter;

    @PostMapping("/sheet")
    // @ApiOperation(value = "단건 주문서 조회")
    public ResponseEntity<BaseResponse<OrderSheetResponse>> findOneOrder(
            @ApiIgnore Principal principal, @RequestBody OrderSheetRequest orderSheetRequest){

        if(principal == null){
            BaseResponse baseResponse = BaseResponse.builder()
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .message("no authorization")
                    .build();
            return new ResponseEntity(baseResponse, HttpStatus.BAD_REQUEST);
        }

        String[] principalInfo = principal.getName().split(" ");
        Long userId = Long.parseLong(principalInfo[0]);

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        OrderSheetDto orderSheetDto = modelMapper.map(orderSheetRequest,OrderSheetDto.class);
        orderSheetDto.setUserId(userId);

        OrderSheetResponse result = orderService.findOneByProductId(orderSheetDto);

        BaseResponse<OrderSheetResponse> response = BaseResponse.<OrderSheetResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message("OK")
                .data(result)
                .build();
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @GetMapping("/carts")
    // @ApiOperation(value = "다건 주문서 조회")
    public ResponseEntity<List<OrderFindOneResponse>> findOrders(@ApiIgnore Principal principal){

        if(principal == null){
            BaseResponse baseResponse = BaseResponse.builder()
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .message("no authorization")
                    .build();
            return new ResponseEntity(baseResponse, HttpStatus.BAD_REQUEST);
        }

        String[] principalInfo = principal.getName().split(" ");
        Long userId = Long.parseLong(principalInfo[0]);

        List<OrderFindOneResponse> responseList
                = orderService.findCartByUserId(userId);

        return new ResponseEntity(responseList,HttpStatus.OK);
    }

    /**
     * 주문 생성할때 product엔티티에서 product상태가 p0 인경우만 판매되게 하는 코드 넣어야 함
     * @param orderRequest
     * @return
     */
    @PostMapping()
    public ResponseEntity<BaseResponse> createOrder(
            @ApiIgnore Principal principal, @RequestBody OrderRequest orderRequest){

        if(principal == null){
            BaseResponse baseResponse = BaseResponse.builder()
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .message("no authorization")
                    .build();
            return new ResponseEntity(baseResponse, HttpStatus.BAD_REQUEST);
        }

        String[] principalInfo = principal.getName().split(" ");
        Long userId = Long.parseLong(principalInfo[0]);

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        OrderDto orderDto = OrderDto.builder()
                .orderRecipientName(orderRequest.getOrderRecipientName())
                .orderRequest(orderRequest.getOrderRequest())
                .orderPhone(orderRequest.getOrderPhone())
                .orderAddress(orderRequest.getOrderAddress())
                .userId(userId)
                .orderSerial(String.valueOf((new Date()).getTime()))
                .productList(new ArrayList<>())
                .merchant_uid(orderRequest.getMerchant_uid())
                .imp_uid(orderRequest.getImp_uid())
                .paid_amount(orderRequest.getPaid_amount())
                .build();

        for(OrderProductRequest order : orderRequest.getProductList()){
            OrderProductDto orderProductDto = OrderProductDto.builder()
                    .productQty(order.getProductQty())
                    .productId(order.getProductId())
                    .productPrice(order.getProductPrice())
                    .build();
            orderDto.getProductList().add(orderProductDto);
        }
        log.info("controller 단 orderDto : "+orderDto.getProductList().toString());
        try{
            tcc.placeOrder(orderDto);
        } catch (RuntimeException e){
            e.printStackTrace();
            BaseResponse response = BaseResponse.builder()
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .message("주문 실패")
                    .build();
            PaymentVo paymentVo = PaymentVo.builder()
                    .imp_uid(orderDto.getImp_uid())
                    .merchant_uid(orderDto.getMerchant_uid())
                    .paid_amount(orderDto.getPaid_amount())
                    .build();
            paymentServiceClient.cancelPayment(paymentVo);
//            String message = "";
//            ObjectMapper objectMapper = new ObjectMapper();
//            try{
//                message = objectMapper.writeValueAsString(orderDto);
//            } catch (JsonProcessingException ex) {
//                throw new RuntimeException(ex);
//            }
//            productOrderChannelAdapter.producer(message);
            return new ResponseEntity(response,HttpStatus.BAD_REQUEST);
        }

        //orderService.createOrder(orderDto);

        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * 넛지 포인트를 위한 주문 메서드
     * @param principal
     * @param orderRequest
     * @return
     */
    @PostMapping("/point")
    public ResponseEntity<BaseResponse> createPointOrder(
            @ApiIgnore Principal principal, @RequestBody PointOrderRequest orderRequest){

        if(principal == null){
            BaseResponse baseResponse = BaseResponse.builder()
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .message("no authorization")
                    .build();
            return new ResponseEntity(baseResponse, HttpStatus.BAD_REQUEST);
        }

        String[] principalInfo = principal.getName().split(" ");
        Long userId = Long.parseLong(principalInfo[0]);

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        OrderDto orderDto = OrderDto.builder()
                .orderRecipientName(orderRequest.getOrderRecipientName())
                .orderRequest(orderRequest.getOrderRequest())
                .orderPhone(orderRequest.getOrderPhone())
                .orderAddress(orderRequest.getOrderAddress())
                .imp_uid(orderRequest.getImp_uid())
                .merchant_uid(orderRequest.getMerchant_uid())
                .paid_amount(orderRequest.getPaid_amount())
                .userId(userId)
                .orderSerial(String.valueOf((new Date()).getTime()))
                .productList(new ArrayList<>())
                .build();

        for(OrderProductRequest order : orderRequest.getProductList()){
            OrderProductDto orderProductDto = OrderProductDto.builder()
                    .productQty(order.getProductQty())
                    .productId(order.getProductId())
                    .productPrice(order.getProductPrice())
                    .build();
            orderDto.getProductList().add(orderProductDto);
        }

        try{
            tcc.placePointOrder(orderDto,orderRequest.getFeedNumber());
        } catch (RuntimeException e){
            e.printStackTrace();
            BaseResponse response = BaseResponse.builder()
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .message("주문 실패")
                    .build();
            PaymentVo paymentVo = PaymentVo.builder()
                    .imp_uid(orderDto.getImp_uid())
                    .merchant_uid(orderDto.getMerchant_uid())
                    .paid_amount(orderDto.getPaid_amount())
                    .build();
            paymentServiceClient.cancelPayment(paymentVo);
//            String message = "";
//            ObjectMapper objectMapper = new ObjectMapper();
//            try{
//                message = objectMapper.writeValueAsString(orderDto);
//            } catch (JsonProcessingException ex) {
//                throw new RuntimeException(ex);
//            }
//            productOrderChannelAdapter.producer(message);
            return new ResponseEntity(response,HttpStatus.BAD_REQUEST);
        }

        //orderService.createOrder(orderDto);
        BaseResponse baseResponse = BaseResponse.builder()
                .httpStatus(HttpStatus.OK)
                .message("주문 성공")
                .build();
        return new ResponseEntity(baseResponse,HttpStatus.OK);
    }


    @GetMapping("/list")
    // @ApiOperation(value = "유저 주문 내역 조회")
    public ResponseEntity<BaseResponse<OrderUserResponseListResponse>> findUserAllOrders(
            @ApiIgnore Principal principal, @RequestParam Integer pageNumber){

        if(principal == null){
            BaseResponse baseResponse = BaseResponse.builder()
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .message("no authorization")
                    .build();
            return new ResponseEntity(baseResponse, HttpStatus.BAD_REQUEST);
        }

        String[] principalInfo = principal.getName().split(" ");
        String userId = principalInfo[0];

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        OrderUserFindDto orderUserFindDto = new OrderUserFindDto();
        orderUserFindDto.setUserId(userId);
        orderUserFindDto.setPageNumber(pageNumber);
        OrderUserResponseListResponse responses  = orderService.findUserOrders(orderUserFindDto);
        BaseResponse response = BaseResponse.builder()
                .httpStatus(HttpStatus.OK)
                .message("OK")
                .data(responses)
                .build();
        return new ResponseEntity(response,HttpStatus.OK);
    }

    @GetMapping("/list/{order-no}")
    // @ApiOperation(value = "유저 주문 상세 조회")
    public ResponseEntity<BaseResponse<OrderUserDetailResponse>> findSellerOrderDetail(
            @PathVariable(name = "order-no") String orderSerial){
        OrderUserDetailResponse detailResponse = orderService.findUserOrderDetail(orderSerial);
        BaseResponse response = BaseResponse.builder()
                .httpStatus(HttpStatus.OK)
                .message("OK")
                .data(detailResponse)
                .build();
        return new ResponseEntity(response,HttpStatus.OK);
    }


    @PostMapping("/claim/cancel")
    // @ApiOperation(value = "취소 생성" )
    public ResponseEntity<BaseResponse<Boolean>> createCancel(
            @ApiIgnore Principal principal, @RequestBody RefundRequest refundRequest){

        if(principal == null){
            BaseResponse baseResponse = BaseResponse.builder()
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .message("no authorization")
                    .build();
            return new ResponseEntity(baseResponse, HttpStatus.BAD_REQUEST);
        }

        String[] principalInfo = principal.getName().split(" ");
        Long userId = Long.parseLong(principalInfo[0]);

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        RefundDto refundDto = modelMapper.map(refundRequest, RefundDto.class);
        refundDto.setUserId(userId);
        Boolean result = orderService.createCancel(refundDto);
        if(result.booleanValue()){
            BaseResponse response = BaseResponse.builder()
                    .httpStatus(HttpStatus.OK)
                    .message("OK")
                    .data(result)
                    .build();
            return new ResponseEntity(response,HttpStatus.OK);
        }
        BaseResponse response = BaseResponse.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message("BAD_REQUEST")
                .data(result)
                .build();
        return new ResponseEntity(response,HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/claim/refund")
    // @ApiOperation(value = "반품 생성" )
    public ResponseEntity<BaseResponse<Boolean>> createRefund(
            @ApiIgnore Principal principal, @RequestBody RefundRequest refundRequest){

        if(principal == null){
            BaseResponse baseResponse = BaseResponse.builder()
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .message("no authorization")
                    .build();
            return new ResponseEntity(baseResponse, HttpStatus.BAD_REQUEST);
        }

        String[] principalInfo = principal.getName().split(" ");
        Long userId = Long.parseLong(principalInfo[0]);

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        RefundDto refundDto = modelMapper.map(refundRequest, RefundDto.class);
        refundDto.setUserId(userId);
        boolean result = orderService.createRefund(refundDto);
        if(result){
            BaseResponse response = BaseResponse.builder()
                    .httpStatus(HttpStatus.OK)
                    .message("OK")
                    .data(Boolean.valueOf(result))
                    .build();
        }
        BaseResponse response = BaseResponse.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .message("BAD_REQUEST")
                .data(Boolean.valueOf(result))
                .build();
        return new ResponseEntity(response,HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/claim/list")
    // @ApiOperation(value = "유저 취소/반품 내역 조회")
    public ResponseEntity<BaseResponse<OrderRefundResultResponse>> findUserClaims(
            @ApiIgnore Principal principal, @RequestParam Integer pageNumber){


        if(principal == null){
            BaseResponse baseResponse = BaseResponse.builder()
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .message("no authorization")
                    .build();
            return new ResponseEntity(baseResponse, HttpStatus.BAD_REQUEST);
        }

        String[] principalInfo = principal.getName().split(" ");
        String userId = principalInfo[0];

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        OrderUserFindDto orderUserFindDto = new OrderUserFindDto();
        orderUserFindDto.setUserId(userId);
        orderUserFindDto.setPageNumber(pageNumber);
        OrderRefundResultResponse responseList = orderService.findUserClaims(orderUserFindDto);
        BaseResponse<OrderRefundResultResponse> response = BaseResponse.<OrderRefundResultResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message("OK")
                .data(responseList)
                .build();
        return new ResponseEntity(response,HttpStatus.OK);
    }

}
