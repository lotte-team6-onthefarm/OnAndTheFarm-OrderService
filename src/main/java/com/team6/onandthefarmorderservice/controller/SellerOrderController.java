package com.team6.onandthefarmorderservice.controller;


import com.team6.onandthefarmorderservice.dto.OrderDeliveryDto;
import com.team6.onandthefarmorderservice.dto.OrderSellerDetailDto;
import com.team6.onandthefarmorderservice.dto.OrderSellerFindDto;
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
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/orders")
/**(value = "주문",description = "주문 상태\n" +
        " * activated(os0) : 주문완료\n" +
        " * canceled(os1) : 주문취소\n" +
        " * refundRequest(os2) : 반품신청\n" +
        " * refundCompleted(os3) : 반품확정\n" +
        " * deliveryProgress(os4) : 배송 중\n" +
        " * deliveryCompleted(os5) : 배송 완료")
 **/
public class SellerOrderController {
    private final OrderService orderService;

    /**
     * 셀러의 경우 주문당 여러 제품을 한번에 보여주어야 함
     * orderId : ListProduct정보
     * @param map
     * @return
     */
    @GetMapping("/list")
    // @ApiOperation(value = "셀러 주문 내역 조회")
    public ResponseEntity<BaseResponse<OrderSellerResponseListResponse>> findSellerAllOrders(
            @ApiIgnore Principal principal, @RequestParam Map<String,String> map){

        if(principal == null){
            BaseResponse baseResponse = BaseResponse.builder()
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .message("no authorization")
                    .build();
            return new ResponseEntity(baseResponse, HttpStatus.BAD_REQUEST);
        }

        String[] principalInfo = principal.getName().split(" ");
        String sellerId = principalInfo[0];

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        OrderSellerRequest orderSellerRequest = OrderSellerRequest.builder()
                .sellerId(sellerId)
                .startDate(map.get("startDate"))
                .endDate(map.get("endDate"))
                .pageNumber(Integer.valueOf(map.get("pageNumber")))
                .ordersStatus(map.get("ordersStatus"))
                .build();
        OrderSellerFindDto orderSellerFindDto = modelMapper.map(orderSellerRequest, OrderSellerFindDto.class);
        orderSellerFindDto.setSellerId(sellerId);

        OrderSellerResponseListResponse responses  = orderService.findSellerOrders(orderSellerFindDto);
        BaseResponse response = BaseResponse.builder()
                .httpStatus(HttpStatus.OK)
                .message("OK")
                .data(responses)
                .build();
        return new ResponseEntity(response,HttpStatus.OK);
    }


    @GetMapping("/list/detail")
    // @ApiOperation(value = "셀러 주문 상세 조회")
    public ResponseEntity<BaseResponse<OrderUserDetailResponse>> findSellerOrderDetail(
            @ApiIgnore Principal principal,@RequestParam Map<String,String> map){

        if(principal == null){
            BaseResponse baseResponse = BaseResponse.builder()
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .message("no authorization")
                    .build();
            return new ResponseEntity(baseResponse, HttpStatus.BAD_REQUEST);
        }

        String[] principalInfo = principal.getName().split(" ");
        String sellerId = principalInfo[0];

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        OrderSellerDetailRequest orderSellerDetailRequest = OrderSellerDetailRequest.builder()
                .orderSerial(map.get("orderSerial"))
                .sellerId(sellerId)
                .build();
        OrderSellerDetailDto orderSellerDetailDto = modelMapper.map(orderSellerDetailRequest , OrderSellerDetailDto.class);
        orderSellerDetailDto.setSellerId(sellerId);

        OrderUserDetailResponse detailResponse = orderService.findSellerOrderDetail(orderSellerDetailDto);

        BaseResponse response = BaseResponse.builder()
                .httpStatus(HttpStatus.OK)
                .message("OK")
                .data(detailResponse)
                .build();
        return new ResponseEntity(response,HttpStatus.OK);
    }


    @GetMapping("/claim/list")
    //@ApiOperation(value = "셀러 취소/반품 내역 조회")
    public ResponseEntity<BaseResponse<OrderSellerResultResponse>> findSellerClaims(
            @ApiIgnore Principal principal,@RequestParam Map<String,String> map){

        if(principal == null){
            BaseResponse baseResponse = BaseResponse.builder()
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .message("no authorization")
                    .build();
            return new ResponseEntity(baseResponse, HttpStatus.BAD_REQUEST);
        }

        String[] principalInfo = principal.getName().split(" ");
        String sellerId = principalInfo[0];
        OrderSellerRequest orderSellerRequest = OrderSellerRequest.builder()
                .sellerId(sellerId)
                .startDate(map.get("startDate"))
                .endDate(map.get("endDate"))
                .pageNumber(Integer.valueOf(map.get("pageNumber")))
                .ordersStatus(map.get("ordersStatus"))
                .build();
        OrderSellerResultResponse responseList = orderService.findSellerClaims(orderSellerRequest);
        BaseResponse<OrderSellerResultResponse> response = BaseResponse.<OrderSellerResultResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message("OK")
                .data(responseList)
                .build();
        return new ResponseEntity(response,HttpStatus.OK);
    }

    @GetMapping("/claim/list/{orderProduct-no}")
    // @ApiOperation(value = "반품 상세 조회")
    public ResponseEntity<BaseResponse<RefundDetailResponse>> findSellerClaimDetail(@PathVariable(name = "orderProduct-no") String orderProductId){
        RefundDetailResponse refundDetailResponse = orderService.findRefundDetail(Long.valueOf(orderProductId));
        BaseResponse response = BaseResponse.builder()
                .httpStatus(HttpStatus.OK)
                .message("OK")
                .data(refundDetailResponse)
                .build();
        return new ResponseEntity(response,HttpStatus.OK);
    }

    @PostMapping("/claim/list/{orderProduct-no}")
    // @ApiOperation(value = "반품 확정")
    public ResponseEntity<BaseResponse> claimConform(@PathVariable(name = "orderProduct-no") String orderProductId){
        Boolean result = orderService.conformRefund(Long.valueOf(orderProductId));
        BaseResponse response = BaseResponse.builder()
                .httpStatus(HttpStatus.OK)
                .message("OK")
                .data(result.booleanValue())
                .build();
        return new ResponseEntity(response,HttpStatus.OK);
    }

    @PostMapping("/delivery")
    // @ApiOperation(value = "배송 시작 처리")
    public ResponseEntity<BaseResponse> deliveryStart(@RequestBody OrderDeliveryRequest orderDeliveryRequest){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        OrderDeliveryDto orderDeliveryDto = modelMapper.map(orderDeliveryRequest, OrderDeliveryDto.class);
        Boolean result = orderService.deliveryStart(orderDeliveryDto);
        BaseResponse response = BaseResponse.builder()
                .httpStatus(HttpStatus.OK)
                .message("OK")
                .data(result.booleanValue())
                .build();
        return new ResponseEntity(response,HttpStatus.OK);
    }

    @PostMapping("/delivery/{order-no}")
    // @ApiOperation(value = "배송 완료 처리")
    public ResponseEntity<BaseResponse> deliveryConform(@PathVariable(name = "order-no") String orderSerial){
        Boolean result = orderService.deliveryConform(orderSerial);
        BaseResponse response = BaseResponse.builder()
                .httpStatus(HttpStatus.OK)
                .message("OK")
                .data(result.booleanValue())
                .build();
        return new ResponseEntity(response,HttpStatus.OK);
    }

    @GetMapping("/condition")
    // @ApiOperation(value = "셀러페이지의 통계를 위한 상품/주문/배송/qna 현황")
    public ResponseEntity<BaseResponse<OrdersConditionResponse>> findOrdersConditions(@ApiIgnore Principal principal){

        if(principal == null){
            BaseResponse baseResponse = BaseResponse.builder()
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .message("no authorization")
                    .build();
            return new ResponseEntity(baseResponse, HttpStatus.BAD_REQUEST);
        }

        String[] principalInfo = principal.getName().split(" ");
        Long sellerId = Long.parseLong(principalInfo[0]);

        OrdersConditionResponse ordersConditionResponse = orderService.findOrdersCondition(sellerId);

        BaseResponse response = BaseResponse.builder()
                .httpStatus(HttpStatus.OK)
                .message("OK")
                .data(ordersConditionResponse)
                .build();
        return new ResponseEntity(response,HttpStatus.OK);
    }


}
