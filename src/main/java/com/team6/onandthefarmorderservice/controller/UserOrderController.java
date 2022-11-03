package com.team6.onandthefarmorderservice.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team6.onandthefarmorderservice.TCC;
import com.team6.onandthefarmorderservice.dto.*;
import com.team6.onandthefarmorderservice.service.OrderService;
import com.team6.onandthefarmorderservice.utils.BaseResponse;
import com.team6.onandthefarmorderservice.vo.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
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
    public ResponseEntity createOrder(
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
                .feedNumber(orderRequest.getFeedNumber())
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
            tcc.placeOrder(orderDto);
        } catch (RuntimeException e){
            e.printStackTrace();
//            String message = "";
//            ObjectMapper objectMapper = new ObjectMapper();
//            try{
//                message = objectMapper.writeValueAsString(orderDto);
//            } catch (JsonProcessingException ex) {
//                throw new RuntimeException(ex);
//            }
//            productOrderChannelAdapter.producer(message);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        //orderService.createOrder(orderDto);

        return new ResponseEntity(HttpStatus.OK);
    }


    @PostMapping("/list")
    // @ApiOperation(value = "유저 주문 내역 조회")
    public ResponseEntity<BaseResponse<OrderUserResponseListResponse>> findUserAllOrders(
            @ApiIgnore Principal principal, @RequestBody OrderUserRequest orderUserRequest){

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
        OrderUserFindDto orderUserFindDto = modelMapper.map(orderUserRequest, OrderUserFindDto.class);
        orderUserFindDto.setUserId(userId);
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

    @PostMapping("/claim/list")
    // @ApiOperation(value = "유저 취소/반품 내역 조회")
    public ResponseEntity<BaseResponse<OrderRefundResultResponse>> findUserClaims(
            @ApiIgnore Principal principal, @RequestBody OrderUserRequest orderUserRequest){

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
        OrderUserFindDto orderUserFindDto = modelMapper.map(orderUserRequest,OrderUserFindDto.class);
        orderUserFindDto.setUserId(userId);
        OrderRefundResultResponse responseList = orderService.findUserClaims(orderUserFindDto);
        BaseResponse<OrderRefundResultResponse> response = BaseResponse.<OrderRefundResultResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message("OK")
                .data(responseList)
                .build();
        return new ResponseEntity(response,HttpStatus.OK);
    }

}
