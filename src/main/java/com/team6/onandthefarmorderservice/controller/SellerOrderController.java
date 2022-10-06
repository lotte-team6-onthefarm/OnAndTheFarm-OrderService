package com.team6.onandthefarmorderservice.controller;

import com.team6.onandthefarmorderservice.dto.OrderDeliveryDto;
import com.team6.onandthefarmorderservice.dto.OrderSellerDetailDto;
import com.team6.onandthefarmorderservice.dto.OrderSellerFindDto;
import com.team6.onandthefarmorderservice.service.OrderService;
import com.team6.onandthefarmorderservice.utils.BaseResponse;
import com.team6.onandthefarmorderservice.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/seller/orders")
@Api(value = "주문",description = "주문 상태\n" +
        " * activated(os0) : 주문완료\n" +
        " * canceled(os1) : 주문취소\n" +
        " * refundRequest(os2) : 반품신청\n" +
        " * refundCompleted(os3) : 반품확정\n" +
        " * deliveryProgress(os4) : 배송 중\n" +
        " * deliveryCompleted(os5) : 배송 완료")
public class SellerOrderController {
    private OrderService orderService;

    @Autowired
    public SellerOrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    /**
     * 셀러의 경우 주문당 여러 제품을 한번에 보여주어야 함
     * orderId : ListProduct정보
     * @param orderSellerRequest
     * @return
     */
    @PostMapping("/list")
    @ApiOperation(value = "셀러 주문 내역 조회")
    public ResponseEntity<BaseResponse<List<OrderSellerResponseList>>> findSellerAllOrders(
            @ApiIgnore Principal principal, @RequestBody OrderSellerRequest orderSellerRequest){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        orderSellerRequest.setSellerId(principal.getName());
        OrderSellerFindDto orderSellerFindDto = modelMapper.map(orderSellerRequest, OrderSellerFindDto.class);
        orderSellerFindDto.setSellerId(principal.getName());

        List<OrderSellerResponseList> responses  = orderService.findSellerOrders(orderSellerFindDto);
        BaseResponse response = BaseResponse.builder()
                .httpStatus(HttpStatus.OK)
                .message("OK")
                .data(responses)
                .build();
        return new ResponseEntity(response,HttpStatus.OK);
    }


    @PostMapping("/list/detail")
    @ApiOperation(value = "셀러 주문 상세 조회")
    public ResponseEntity<BaseResponse<OrderUserDetailResponse>> findSellerOrderDetail(
            @ApiIgnore Principal principal,@RequestBody OrderSellerDetailRequest orderSellerDetailRequest){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        orderSellerDetailRequest.setSellerId(principal.getName());
        OrderSellerDetailDto orderSellerDetailDto = modelMapper.map(orderSellerDetailRequest , OrderSellerDetailDto.class);
        orderSellerDetailDto.setSellerId(principal.getName());

        OrderUserDetailResponse detailResponse = orderService.findSellerOrderDetail(orderSellerDetailDto);

        BaseResponse response = BaseResponse.builder()
                .httpStatus(HttpStatus.OK)
                .message("OK")
                .data(detailResponse)
                .build();
        return new ResponseEntity(response,HttpStatus.OK);
    }


    @PostMapping("/claim/list")
    @ApiOperation(value = "셀러 취소/반품 내역 조회")
    public ResponseEntity<BaseResponse<List<OrderSellerResponse>>> findSellerClaims(
            @ApiIgnore Principal principal,@RequestBody OrderSellerRequest orderSellerRequest){
        orderSellerRequest.setSellerId(principal.getName());
        List<OrderSellerResponse> responseList = orderService.findSellerClaims(orderSellerRequest);
        BaseResponse<List<OrderSellerResponse>> response = BaseResponse.<List<OrderSellerResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message("OK")
                .data(responseList)
                .build();
        return new ResponseEntity(response,HttpStatus.OK);
    }

    @GetMapping("/claim/list/{orderProduct-no}")
    @ApiOperation(value = "취소/반품 상세 조회")
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
    @ApiOperation(value = "반품 확정")
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
    @ApiOperation(value = "배송 시작 처리")
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
    @ApiOperation(value = "배송 완료 처리")
    public ResponseEntity<BaseResponse> deliveryConform(@PathVariable(name = "order-no") String orderSerial){
        Boolean result = orderService.deliveryConform(orderSerial);
        BaseResponse response = BaseResponse.builder()
                .httpStatus(HttpStatus.OK)
                .message("OK")
                .data(result.booleanValue())
                .build();
        return new ResponseEntity(response,HttpStatus.OK);
    }


}