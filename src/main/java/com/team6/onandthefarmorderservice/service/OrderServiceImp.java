package com.team6.onandthefarmorderservice.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team6.onandthefarmorderservice.dto.*;
import com.team6.onandthefarmorderservice.entity.OrderProduct;
import com.team6.onandthefarmorderservice.entity.Orders;
import com.team6.onandthefarmorderservice.entity.Refund;
import com.team6.onandthefarmorderservice.feignclient.CartServiceClient;
import com.team6.onandthefarmorderservice.feignclient.ProductServiceClient;
import com.team6.onandthefarmorderservice.feignclient.SnsServiceClient;
import com.team6.onandthefarmorderservice.feignclient.UserServiceClient;
import com.team6.onandthefarmorderservice.kafka.OrderChannelAdapter;
import com.team6.onandthefarmorderservice.kafka.vo.Field;
import com.team6.onandthefarmorderservice.kafka.vo.KafkaOrderDto;
import com.team6.onandthefarmorderservice.kafka.vo.Payload;
import com.team6.onandthefarmorderservice.kafka.vo.Schema;
import com.team6.onandthefarmorderservice.repository.OrderProductRepository;
import com.team6.onandthefarmorderservice.repository.OrderRepository;
import com.team6.onandthefarmorderservice.repository.RefundRepository;
import com.team6.onandthefarmorderservice.utils.DateUtils;
import com.team6.onandthefarmorderservice.vo.*;
import com.team6.onandthefarmorderservice.vo.feignclient.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImp implements OrderService {

    private final ProductServiceClient productServiceClient;

    private final UserServiceClient userServiceClient;

    private final CartServiceClient cartServiceClient;

    private final SnsServiceClient snsServiceClient;

    private final OrderRepository orderRepository;

    private final OrderProductRepository orderProductRepository;

    private final int pageContentNumber = 8;

    private final RefundRepository refundRepository;

    private final DateUtils dateUtils;

    private final Environment env;

    private final CircuitBreakerFactory circuitbreakerFactory;

    private final OrderChannelAdapter orderChannelAdapter;

    List<Field> fields = Arrays.asList(new Field("int64",false,"orders_id"),
            new Field("string",true,"orders_address"),
            new Field("string",true,"orders_date"),
            new Field("string",true,"orders_delivery_company"),
            new Field("string",true,"orders_delivery_date"),
            new Field("string",true,"orders_delivery_waybill_number"),
            new Field("string",true,"orders_phone"),
            new Field("string",true,"orders_recipient_name"),
            new Field("string",true,"orders_request"),
            new Field("int64",true,"orders_seller_id"),
            new Field("string",true,"orders_serial"),
            new Field("string",true,"orders_status"),
            new Field("int32",true,"orders_total_price"),
            new Field("int64",true,"user_id"));
    Schema schema = Schema.builder()
            .type("struct")
            .fields(fields)
            .optional(false)
            .name("orders")
            .build();


    public Boolean createOrder(OrderDto orderDto){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        CircuitBreaker circuitbreaker = circuitbreakerFactory.create("circuitbreaker");
        boolean stockCheck = checkStock(orderDto,circuitbreaker);
        if(!stockCheck){ // 재고 체크
            return Boolean.FALSE;
        }

        // productId->sellerId를 찾기
        /*
            key : productId
            value : sellerId
            prodSeller는 담을 공간
         */

//        Orders orders = Orders.builder()
//                .ordersPhone(orderDto.getOrderPhone())
//                .ordersAddress(orderDto.getOrderAddress())
//                .ordersRequest(orderDto.getOrderRequest())
//                .ordersRecipientName(orderDto.getOrderRecipientName())
//                .ordersSerial(orderDto.getOrderSerial())
//                .ordersDate(dateUtils.transDate(env.getProperty("dateutils.format")))
//                .ordersStatus("activated")
//                .userId(orderDto.getUserId())
//                .build(); // 주문 엔티티 생성
//        for(OrderProductDto orderProductDto : orderDto.getProductList()){
//            ProductVo productVo
//                    = circuitbreaker.run(
//                            ()->productServiceClient.findByProductId(orderProductDto.getProductId()),
//                    throwable -> new ProductVo());
//            //ProductVo productVo = productServiceClient.findByProductId(orderProductDto.getProductId());
//            orderProductDto.setProductName(productVo.getProductName());
//            orderProductDto.setProductPrice(productVo.getProductPrice());
//            orderProductDto.setSellerId(productVo.getSellerId());
//            orders.setOrdersSellerId(productVo.getSellerId());
//            orderProductDto.setProductImg(productVo.getProductMainImgSrc()); // 이미지는 나중에
//        }

        Payload payload = Payload.builder()
                .orders_id(Long.valueOf(orderDto.getOrderSerial()))
                .orders_phone(orderDto.getOrderPhone())
                .orders_address(orderDto.getOrderAddress())
                .orders_request(orderDto.getOrderRequest())
                .orders_recipient_name(orderDto.getOrderRecipientName())
                .orders_serial(orderDto.getOrderSerial())
                .orders_date(dateUtils.transDate(env.getProperty("dateutils.format")))
                .orders_status("activated")
                .user_id(orderDto.getUserId())
                .build();

        for(OrderProductDto orderProductDto : orderDto.getProductList()){
            ProductVo productVo
                    = circuitbreaker.run(
                    ()->productServiceClient.findByProductId(orderProductDto.getProductId()),
                    throwable -> new ProductVo());
            //ProductVo productVo = productServiceClient.findByProductId(orderProductDto.getProductId());
            orderProductDto.setProductName(productVo.getProductName());
            orderProductDto.setProductPrice(productVo.getProductPrice());
            orderProductDto.setSellerId(productVo.getSellerId());
            payload.setOrders_seller_id(productVo.getSellerId());
            orderProductDto.setProductImg(productVo.getProductMainImgSrc()); // 이미지는 나중에
        }

        KafkaOrderDto kafkaOrderDto = new KafkaOrderDto(schema,payload);
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try{
            jsonInString = mapper.writeValueAsString(kafkaOrderDto);
        }catch(JsonProcessingException ex){
            ex.printStackTrace();
        }

        orderChannelAdapter.producer(jsonInString);
        //Orders ordersEntity = orderRepository.save(orders); // 주문 생성
        try{
            Thread.sleep(500); // 저장 시간
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        Optional<Orders> ordersEntity = orderRepository.findById(Long.valueOf(orderDto.getOrderSerial()));
        if(ordersEntity.isEmpty()){
            throw new RuntimeException();
        }
        log.info(orderDto.getProductList().toString());
        int totalPrice = 0;
        for(OrderProductDto order : orderDto.getProductList()){
            totalPrice+=order.getProductPrice() * order.getProductQty();
            OrderProduct orderProduct = OrderProduct.builder()
                    .orderProductMainImg(order.getProductImg())
                    .orderProductQty(order.getProductQty())
                    .orderProductPrice(order.getProductPrice())
                    .orderProductName(order.getProductName())
                    .orders(ordersEntity.get())
                    .productId(order.getProductId())
                    .sellerId(order.getSellerId())
                    .orderProductStatus(ordersEntity.get().getOrdersStatus())
                    .orderProductDate(ordersEntity.get().getOrdersDate())
                    .build();
            orderProductRepository.save(orderProduct); // 각각의 주문 상품 생성
        }
        orderRepository.findById(ordersEntity.get().getOrdersId()).get().setOrdersTotalPrice(totalPrice); // 총 주문액 set
        return Boolean.TRUE;
    }
    /**
     * 재고 수량 확인하는 메서드
     * @param orderDto
     * @return true : 주문 가능 / false : 주문 불가능
     */
    public boolean checkStock(OrderDto orderDto, CircuitBreaker circuitbreaker){
        List<OrderProductDto> orderProducts = orderDto.getProductList();
        for(OrderProductDto orderProduct : orderProducts){
            ProductVo productVo
                    = circuitbreaker.run(
                            ()->productServiceClient.findByProductId(orderProduct.getProductId()),
                    throwable -> new ProductVo());
            //ProductVo productVo = productServiceClient.findByProductId(orderProduct.getProductId());
            if(productVo.getProductTotalStock()==null) return false;
            if(productVo.getProductTotalStock()>=orderProduct.getProductQty()){
                return true;
            }
            if(!productVo.getProductStatus().equals("selling")){
                return false;
            }
        }
        return false;
    }
    /**
     * 주문 생성 메서드
     * 주문 생성 시 product 판매 수 늘리기 코드 짜기
     * 장바구니의 is_activate가 false라면 주문후 cart삭제(status를 false로 바꿈)
     * @param orderSheetDto
     */
    public OrderSheetResponse findOneByProductId(OrderSheetDto orderSheetDto){
        CircuitBreaker productCircuitbreaker = circuitbreakerFactory.create("productCircuitbreaker");
        CircuitBreaker userCircuitbreaker = circuitbreakerFactory.create("userCircuitbreaker");

        ProductVo product
                = productCircuitbreaker.run(
                        ()->productServiceClient.findByProductId(orderSheetDto.getProductId()),
                throwable -> new ProductVo());
        //ProductVo product = productServiceClient.findByProductId(orderSheetDto.getProductId());

        if(product.getProductPrice()==null){
            product.setProductPrice(0);
        }

        UserVo user
                = userCircuitbreaker.run(
                        ()->userServiceClient.findByUserId(orderSheetDto.getUserId()),
                throwable -> new UserVo());
        //UserVo user = userServiceClient.findByUserId(orderSheetDto.getUserId());
        log.info("제품 정보  =>  "+ product.toString());
        OrderSheetResponse response = OrderSheetResponse.builder()
                .productId(orderSheetDto.getProductId())
                .productImg(product.getProductMainImgSrc())
                .productName(product.getProductName())
                .productPrice(product.getProductPrice())
                .sellerId(product.getSellerId())
                .productQty(orderSheetDto.getProductQty())
                .productTotalPrice(orderSheetDto.getProductQty()*product.getProductPrice())
                .userAddress(user.getUserAddress())
                .userName(user.getUserName())
                .userPhone(user.getUserPhone())
                .build();
        return response;
    }

    /**
     * 다건 주문서 조회 시 사용되는 메서드
     * @param userId
     * @return
     */

    public List<OrderFindOneResponse> findCartByUserId(Long userId){
        /**
         * 유저id로 카트 정보 + 상품 정보 가져오기
         */
        CircuitBreaker circuitBreaker = circuitbreakerFactory.create("product_circuitbreaker");

        List<CartVo> carts
                = circuitBreaker.run(
                        ()->cartServiceClient.findByUserId(userId),
                throwable -> new ArrayList<>());
        //List<CartVo> carts = cartServiceClient.findByUserId(userId);

        List<OrderFindOneResponse> list = new ArrayList<>();

        for(CartVo cart :carts){
            ProductVo product
                    = circuitBreaker.run(
                            ()->productServiceClient.findByProductId(cart.getProductId()),
                    throwable -> new ProductVo());
            //ProductVo product = productServiceClient.findByProductId(cart.getProductId());
            OrderFindOneResponse response = OrderFindOneResponse.builder()
                    .productQty(cart.getCartQty())
                    .productName(product.getProductName())
                    .productImg(product.getProductMainImgSrc())
                    .productPrice(product.getProductPrice())
                    .productId(product.getProductId())
                    .sellerId(product.getSellerId())
                    .build();
            list.add(response);
        }

        return list;
    }
    /**
     * 셀러의 주문 내역 조회(최근 정렬)를 위해 사용되는 메서드
     * @param orderSellerFindDto : 셀러 ID와 조회할 기간을 가진 DTO
     * @return
     */
    public OrderSellerResponseListResponse findSellerOrders(OrderSellerFindDto orderSellerFindDto){
        CircuitBreaker userCircuitbreaker = circuitbreakerFactory.create("userCircuitbreaker");

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        List<OrderSellerResponseList> responseList = new ArrayList<>();

        List<OrderProduct> orderProductList
                = orderProductRepository.findBySellerIdAndOrderProductStatusAndOrderProductDateBetween(
                Long.valueOf(orderSellerFindDto.getSellerId()),
                orderSellerFindDto.getOrdersStatus(),
                orderSellerFindDto.getStartDate(),
                orderSellerFindDto.getEndDate());

        /*
                sellerId : orderId
         */
        Map<Long, Set<Long>> matching = new HashMap<>();
        for(OrderProduct orderProduct : orderProductList){
            if(Long.valueOf(orderSellerFindDto.getSellerId()).equals(orderProduct.getSellerId())){
                if(!matching.containsKey(orderProduct.getSellerId())){
                    matching.put(orderProduct.getSellerId(),new HashSet<>());
                }
                matching.get(orderProduct.getSellerId()).add(orderProduct.getOrders().getOrdersId());
            }
        }
        if(matching.size()==0)
            return OrderSellerResponseListResponse.builder().responses(new ArrayList<>()).currentPageNum(0).totalPageNum(0).build();
        for(Long orderId : matching.get(Long.valueOf(orderSellerFindDto.getSellerId()))){
            List<OrderSellerResponse> orderResponse = new ArrayList<>();
            int totalPrice = 0;
            Optional<Orders> order = orderRepository.findById(orderId);
            UserVo user
                    = userCircuitbreaker.run(
                            ()->userServiceClient.findByUserId(order.get().getUserId()),
                    throwable -> new UserVo());
            //UserVo user = userServiceClient.findByUserId(order.get().getUserId());

            for(OrderProduct orderProduct : orderProductList){
                if(!orderProduct.getOrders().getOrdersId().equals(orderId)) continue;
                OrderSellerResponse orderSellerResponse = OrderSellerResponse.builder()
                        .userName(user.getUserName())
                        .orderProductName(orderProduct.getOrderProductName())
                        .orderProductMainImg(orderProduct.getOrderProductMainImg())
                        .orderProductPrice(orderProduct.getOrderProductPrice())
                        .orderProductQty(orderProduct.getOrderProductQty())
                        .ordersDate(order.get().getOrdersDate())
                        .ordersSerial(order.get().getOrdersSerial())
                        .orderProductStatus(orderProduct.getOrderProductStatus())
                        .build();
                if(orderProduct.getOrderProductStatus().equals("deliveryProgress")||
                        orderProduct.getOrderProductStatus().equals("deliveryCompleted")){
                    orderSellerResponse.setOrderProductDeliveryWaybillNumber(order.get().getOrdersDeliveryWaybillNumber());
                    orderSellerResponse.setOrderProductDeliveryCompany(order.get().getOrdersDeliveryCompany());
                    orderSellerResponse.setOrderProductDeliveryDate(order.get().getOrdersDeliveryDate());
                }
                totalPrice+=orderProduct.getOrderProductPrice()*orderProduct.getOrderProductQty();
                orderResponse.add(orderSellerResponse);
            }
            OrderSellerResponseList orderSellerResponseList = new OrderSellerResponseList();
            orderSellerResponseList.setOrderSellerResponses(orderResponse);
            orderSellerResponseList.setOrderTotalPrice(totalPrice);
            orderSellerResponseList.setOrderDate(order.get().getOrdersDate());
            if(orderSellerResponseList.getOrderSellerResponses().size()!=0){
                responseList.add(orderSellerResponseList);
            }
        }



        /**
         * 아래가 정렬 및 페이징처리 코드
         */

        OrderSellerResponseListResponse resultResponse = new OrderSellerResponseListResponse();
        if(!orderSellerFindDto.getOrdersStatus().equals("activated")) {
            responseList.sort((o1, o2) -> {
                int result = o2.getOrderDate().compareTo(o1.getOrderDate());
                return result;
            });
        }

        int startIndex = orderSellerFindDto.getPageNumber()*pageContentNumber;

        int size = responseList.size();


        if(size<startIndex+pageContentNumber){
            resultResponse.setResponses(responseList.subList(startIndex,size));
            resultResponse.setCurrentPageNum(orderSellerFindDto.getPageNumber());
            if(size%pageContentNumber!=0){
                resultResponse.setTotalPageNum((size/pageContentNumber)+1);
            }
            else{
                resultResponse.setTotalPageNum(size/pageContentNumber);
            }
            return resultResponse;
        }

        resultResponse.setResponses(responseList.subList(startIndex,startIndex+pageContentNumber));
        resultResponse.setCurrentPageNum(orderSellerFindDto.getPageNumber());
        if(size%pageContentNumber!=0){
            resultResponse.setTotalPageNum((size/pageContentNumber)+1);
        }
        else{
            resultResponse.setTotalPageNum(size/pageContentNumber);
        }
        return resultResponse;
    }

    /**
     * 유저가 주문 내역을 조회 할때 사용되는 메서드
     * userId로 자신의 주문 다 가져오고 -> orderProduct테이블에서 orderId로 묶은 뒤 보내기
     * @param orderUserFindDto
     * @return
     */
    public OrderUserResponseListResponse findUserOrders(OrderUserFindDto orderUserFindDto){
        CircuitBreaker userCircuitbreaker = circuitbreakerFactory.create("userCircuitbreaker");

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        List<OrderUserResponseList> response = new ArrayList<>();

        UserVo user
                = userCircuitbreaker.run(
                        ()->userServiceClient
                                .findByUserId(Long.valueOf(orderUserFindDto.getUserId())),
                throwable -> new UserVo());
        //UserVo user = userServiceClient.findByUserId(Long.valueOf(orderUserFindDto.getUserId()));

        List<Orders> myOrders = orderRepository.findByUserId(user.getUserId());

        for(Orders order : myOrders){
            int totalPrice = 0;

            OrderUserResponseList orderUserResponseList = new OrderUserResponseList();
            List<OrderProduct> userOrdersO1
                    = orderProductRepository.findByOrdersAndOrderProductStatus(order,"activated");
            List<OrderProduct> userOrdersO2
                    = orderProductRepository.findByOrdersAndOrderProductStatus(order,"deliveryProgress");
            List<OrderProduct> userOrdersO3
                    = orderProductRepository.findByOrdersAndOrderProductStatus(order,"deliveryCompleted");

            List<OrderProduct> userOrders = new ArrayList<>();
            userOrders.addAll(userOrdersO1);
            userOrders.addAll(userOrdersO2);
            userOrders.addAll(userOrdersO3);

            List<OrderUserResponse> orderUserResponses = new ArrayList<>();

            for(OrderProduct orderProduct : userOrders){
                OrderUserResponse orderUserResponse = OrderUserResponse.builder()
                        .orderProductStatus(orderProduct.getOrderProductStatus())
                        .orderProductPrice(orderProduct.getOrderProductPrice())
                        .orderProductQty(orderProduct.getOrderProductQty())
                        .orderProductName(orderProduct.getOrderProductName())
                        .orderProductMainImg(orderProduct.getOrderProductMainImg())
                        .productId(orderProduct.getProductId())
                        .orderProductId(orderProduct.getOrderProductId())
                        .build();
                totalPrice+=orderProduct.getOrderProductPrice()*orderProduct.getOrderProductQty();
                orderUserResponses.add(orderUserResponse);
            }
            if(orderUserResponses.isEmpty()) continue;
            orderUserResponseList.setOrderUserResponses(orderUserResponses);
            orderUserResponseList.setOrderTotalPrice(totalPrice);
            orderUserResponseList.setOrdersSerial(order.getOrdersSerial());
            orderUserResponseList.setOrderDate(order.getOrdersDate());
            orderUserResponseList.setOrderProductDeliveryWaybillNumber(order.getOrdersDeliveryWaybillNumber());
            orderUserResponseList.setOrderProductDeliveryCompany(order.getOrdersDeliveryCompany());
            orderUserResponseList.setOrderProductDeliveryDate(order.getOrdersDeliveryDate());
            orderUserResponseList.setOrderStatus(order.getOrdersStatus());
            response.add(orderUserResponseList);
        }

        OrderUserResponseListResponse resultResponse = new OrderUserResponseListResponse();

        response.sort((o1, o2) -> {
            int result = o2.getOrderDate().compareTo(o1.getOrderDate());
            return result;
        });

        int startIndex = orderUserFindDto.getPageNumber()*pageContentNumber;

        int size = response.size();


        if(size<startIndex+pageContentNumber){
            resultResponse.setResponses(response.subList(startIndex,size));
            resultResponse.setCurrentPageNum(orderUserFindDto.getPageNumber());
            if(size%pageContentNumber!=0){
                resultResponse.setTotalPageNum((size/pageContentNumber)+1);
            }
            else{
                resultResponse.setTotalPageNum(size/pageContentNumber);
            }
            return resultResponse;
        }

        resultResponse.setResponses(response.subList(startIndex,startIndex+pageContentNumber));
        resultResponse.setCurrentPageNum(orderUserFindDto.getPageNumber());
        if(size%pageContentNumber!=0){
            resultResponse.setTotalPageNum((size/pageContentNumber)+1);
        }
        else{
            resultResponse.setTotalPageNum(size/pageContentNumber);
        }
        return resultResponse;
    }

    public OrderUserDetailResponse findSellerOrderDetail(OrderSellerDetailDto orderSellerDetailDto){
        Orders order = orderRepository.findByOrdersSerial(orderSellerDetailDto.getOrderSerial());

        int totalPrice = 0;

        OrderUserDetailResponse orderUserDetailResponse = OrderUserDetailResponse.builder()
                .orderAddress(order.getOrdersAddress())
                .orderDate(order.getOrdersDate())
                .orderName(order.getOrdersRecipientName())
                .orderPhone(order.getOrdersPhone())
                .orderRequest(order.getOrdersRequest())
                .orderStatus(order.getOrdersStatus())
                .orderProducts(new ArrayList<>())
                .build();

        List<OrderProduct> orderProducts = orderProductRepository.findByOrders(order);

        for(OrderProduct orderProduct : orderProducts){
            if(!orderProduct.getSellerId().equals(Long.valueOf(orderSellerDetailDto.getSellerId()))) continue;
            OrderFindOneDetailResponse orderFindOneDetailResponse = OrderFindOneDetailResponse.builder()
                    .orderProductId(orderProduct.getOrderProductId())
                    .productImg(orderProduct.getOrderProductMainImg())
                    .productName(orderProduct.getOrderProductName())
                    .productPrice(orderProduct.getOrderProductPrice())
                    .productQty(orderProduct.getOrderProductQty())
                    .build();
            totalPrice+=orderFindOneDetailResponse.getProductPrice()
                    *orderFindOneDetailResponse.getProductQty();
            orderUserDetailResponse.getOrderProducts().add(orderFindOneDetailResponse);
        }

        orderUserDetailResponse.setOrderTotalPrice(totalPrice);

        return orderUserDetailResponse;
    }

    /**
     * 유저가 주문 상세 조회를 할때 사용되는 메서드
     * @param orderSerial
     * @return
     */
    public OrderUserDetailResponse findUserOrderDetail(String orderSerial){
        CircuitBreaker userCircuitbreaker = circuitbreakerFactory.create("userCircuitbreaker");

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        Orders orders = orderRepository.findByOrdersSerial(orderSerial);

        UserVo user
                = userCircuitbreaker.run(
                        ()->userServiceClient.findByUserId(orders.getUserId()),
                throwable -> new UserVo());
        //UserVo user = userServiceClient.findByUserId(orders.getUserId());

        List<OrderProduct> orderProducts = orderProductRepository.findByOrders(orders); // 주문에 대한 모든 제품가져옴

        int totalPrice = 0;

        OrderUserDetailResponse orderUserDetailResponse =
                OrderUserDetailResponse.builder()
                        .orderName(orders.getOrdersRecipientName())
                        .orderAddress(orders.getOrdersAddress())
                        .orderDate(orders.getOrdersDate())
                        .orderPhone(orders.getOrdersPhone())
                        .orderRequest(orders.getOrdersRequest())
                        .orderStatus(orders.getOrdersStatus())
                        .orderProducts(new ArrayList<>())
                        .orderProductDeliveryCompany(orders.getOrdersDeliveryCompany())
                        .orderProductDeliveryDate(orders.getOrdersDeliveryDate())
                        .orderProductDeliveryWaybillNumber(orders.getOrdersDeliveryWaybillNumber())
                        .build();

        for(OrderProduct orderProduct : orderProducts){
            OrderFindOneDetailResponse orderFindOneResponse = OrderFindOneDetailResponse.builder()
                    .productPrice(orderProduct.getOrderProductPrice())
                    .productName(orderProduct.getOrderProductName())
                    .productImg(orderProduct.getOrderProductMainImg())
                    .productQty(orderProduct.getOrderProductQty())
                    .orderProductId(orderProduct.getOrderProductId())
                    .productId(orderProduct.getProductId())
                    .orderProductStatus(orderProduct.getOrderProductStatus())
                    .build();
            totalPrice+=orderFindOneResponse.getProductPrice()*orderFindOneResponse.getProductQty();
            orderUserDetailResponse.getOrderProducts().add(orderFindOneResponse);
        }

        orderUserDetailResponse.setOrderTotalPrice(totalPrice);

        return orderUserDetailResponse;
    }

    /**
     * 아직 써킷브레이커 안붙임
     * 취소 생성해주는 메서드
     * @param refundDto
     * @return
     */
    public Boolean createCancel(RefundDto refundDto){
        Optional<OrderProduct> orderProduct = orderProductRepository.findById(refundDto.getOrderProductId());
        if(!orderProduct.get().getOrderProductStatus().equals("activated")) return Boolean.FALSE;
        orderProduct.get().setOrderProductStatus("canceled"); // 취소상태

        // 모든 주문상품의 상태가 취소처리되면 주문상태 또한 취소상태로 바꾸는 부분
        Orders orders = orderProduct.get().getOrders();
        Optional<Orders> savedOrders = orderRepository.findById(orders.getOrdersId());

        int canceledCount = 0;
        int orderProductsCount = 0;
        List<OrderProduct> productOfSameOrders = orderProductRepository.findOrderProductsByOrders(orders);
        for(OrderProduct oProduct : productOfSameOrders){
            if(oProduct.getOrderProductStatus().equals("canceled")){
                canceledCount++;
            }
            orderProductsCount++;
        }
        if(canceledCount == orderProductsCount) {
            savedOrders.get().setOrdersStatus("canceled");
        }
        /**
         * 분산 트랜잭션 구간
         */
        ProductVo product = productServiceClient.findByProductId(orderProduct.get().getProductId());
        product.setProductTotalStock(product.getProductTotalStock()+orderProduct.get().getOrderProductQty());

        if(orderProduct.get().getOrderProductStatus().equals("canceled")){
            return true;
        }
        return false;
    }

    /**
     * 반품 생성해주는 메서드
     * @param refundDto
     * @return
     */
    public Boolean createRefund(RefundDto refundDto){
        Optional<OrderProduct> orderProduct = orderProductRepository.findById(refundDto.getOrderProductId());
        if(!orderProduct.get().getOrderProductStatus().equals("deliveryCompleted")) return Boolean.FALSE;
        orderProduct.get().setOrderProductStatus("refundRequest"); // 반품신청상태

        // 모든 주문상품의 상태가 환불처리되면 주문상태 또한 환불상태로 바꾸는 부분
        Orders orders = orderProduct.get().getOrders();
        Optional<Orders> savedOrders = orderRepository.findById(orders.getOrdersId());

        int refundCount = 0;
        int orderProductsCount = 0;
        List<OrderProduct> productOfSameOrders = orderProductRepository.findOrderProductsByOrders(orders);
        for(OrderProduct oProduct : productOfSameOrders){
            if(oProduct.getOrderProductStatus().equals("refundRequest")){
                refundCount++;
            }
            orderProductsCount++;
        }
        if(refundCount == orderProductsCount) {
            savedOrders.get().setOrdersStatus("refundRequest");
        }

        Refund refund = Refund.builder()
                .refundContent(refundDto.getRefundDetail())
                .orderProductId(refundDto.getOrderProductId())
                .refundImage(refundDto.getRefundImage())
                .userId(refundDto.getUserId())
                .orders(orders)
                .build();
        refundRepository.save(refund);

        if(orderProduct.get().getOrderProductStatus().equals("refundRequest")){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 셀러 반품/취소 내역 조회해주는 메서드
     * 날짜 조정하는 코드 넣기
     * @param orderSellerRequest
     * @return
     */
    public OrderSellerResultResponse findSellerClaims(OrderSellerRequest orderSellerRequest){
        CircuitBreaker userCircuitbreaker = circuitbreakerFactory.create("userCircuitbreaker");

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        /*
            sellerId로 orderProduct에서 제품 하나씩 찾는다. canceled(os1) refundRequest(os2) 인 상태의 제품들을
         */
        List<OrderProduct> orderProductsSO1 = orderProductRepository.findBySellerIdAndOrderProductStatusAndOrderProductDateBetween(Long.valueOf(orderSellerRequest.getSellerId()),orderSellerRequest.getOrdersStatus(),orderSellerRequest.getStartDate(),orderSellerRequest.getEndDate());
//        List<OrderProduct> orderProductsSO2 = orderProductRepository.findBySellerIdAndOrderProductStatusAndOrderProductDateBetween(Long.valueOf(orderSellerRequest.getSellerId()),"refundRequest",orderSellerRequest.getStartDate(),orderSellerRequest.getEndDate());
//        List<OrderProduct> orderProductsSO3 = orderProductRepository.findBySellerIdAndOrderProductStatusAndOrderProductDateBetween(Long.valueOf(orderSellerRequest.getSellerId()),"refundCompleted",orderSellerRequest.getStartDate(),orderSellerRequest.getEndDate());
//        List<OrderProduct> orderProducts = new ArrayList<>();
//        for(OrderProduct orderProduct : orderProductsSO1){
//            orderProducts.add(orderProduct);
//        }
//        for(OrderProduct orderProduct : orderProductsSO2){
//            orderProducts.add(orderProduct);
//        }
//        for(OrderProduct orderProduct : orderProductsSO3){
//            orderProducts.add(orderProduct);
//        }


        List<OrderSellerResponse> responseList = new ArrayList<>();
        for(OrderProduct orderProduct : orderProductsSO1){
            OrderSellerResponse orderSellerResponse = modelMapper.map(orderProduct,OrderSellerResponse.class);
            Optional<Orders> orders = orderRepository.findById(orderProduct.getOrders().getOrdersId());
            UserVo user
                    = userCircuitbreaker.run(
                            ()->userServiceClient.findByUserId(orders.get().getUserId()),
                    throwable -> new UserVo());
            //UserVo user = userServiceClient.findByUserId(orders.get().getUserId());

            orderSellerResponse.setOrdersDate(orders.get().getOrdersDate());
            orderSellerResponse.setOrdersSerial(orders.get().getOrdersSerial());
            orderSellerResponse.setUserProfile(user.getUserProfileImg());
            orderSellerResponse.setUserName(user.getUserName());
            orderSellerResponse.setOrderProductId(orderProduct.getOrderProductId());
            orderSellerResponse.setOrderTotalPrice(
                    orderProduct.getOrderProductPrice()*orderProduct.getOrderProductQty());
            responseList.add(orderSellerResponse);
        }

        OrderSellerResultResponse resultResponse = new OrderSellerResultResponse();

        responseList.sort((o1, o2) -> {
            int result = o2.getOrdersDate().compareTo(o1.getOrdersDate());
            return result;
        });

        int startIndex = orderSellerRequest.getPageNumber()*pageContentNumber;

        int size = responseList.size();

        if(size<startIndex+pageContentNumber){
            resultResponse.setResponses(responseList.subList(startIndex,size));
            resultResponse.setCurrentPageNum(orderSellerRequest.getPageNumber());
            if(size%pageContentNumber!=0){
                resultResponse.setTotalPageNum((size/pageContentNumber)+1);
            }
            else{
                resultResponse.setTotalPageNum(size/pageContentNumber);
            }
            return resultResponse;
        }

        resultResponse.setResponses(responseList.subList(startIndex,startIndex+pageContentNumber));
        resultResponse.setCurrentPageNum(orderSellerRequest.getPageNumber());
        if(size%pageContentNumber!=0){
            resultResponse.setTotalPageNum((size/pageContentNumber)+1);
        }
        else{
            resultResponse.setTotalPageNum(size/pageContentNumber);
        }
        return resultResponse;
    }

    /**
     * 유저 반품/취소 내역 조회
     * @param orderUserFindDto
     * @return
     */
    public OrderRefundResultResponse findUserClaims(OrderUserFindDto orderUserFindDto){
        CircuitBreaker userCircuitbreaker = circuitbreakerFactory.create("userCircuitbreaker");

        List<OrderRefundResponse> responses = new ArrayList<>();

        List<Refund> refunds = refundRepository.findByUserId(Long.valueOf(orderUserFindDto.getUserId()));

        UserVo user
                = userCircuitbreaker.run(
                    ()->userServiceClient.findByUserId(Long.valueOf(orderUserFindDto.getUserId())),
                throwable -> new UserVo());
        //UserVo user = userServiceClient.findByUserId(Long.valueOf(orderUserFindDto.getUserId()));

        List<Orders> orders = orderRepository.findByUserId(user.getUserId());

        for(Orders order : orders){
            List<OrderProduct> cancels
                    = orderProductRepository.findByOrdersAndOrderProductStatus(order,"canceled");
            for(OrderProduct orderProduct : cancels){
                OrderRefundResponse response = OrderRefundResponse.builder()
                        .productPrice(orderProduct.getOrderProductPrice())
                        .productQty(orderProduct.getOrderProductQty())
                        .productTotalPrice(
                                orderProduct.getOrderProductPrice() * orderProduct.getOrderProductQty())
                        .productStatus(orderProduct.getOrderProductStatus())
                        .productName(orderProduct.getOrderProductName())
                        .productImage(orderProduct.getOrderProductMainImg())
                        .orderDate(orderProduct.getOrderProductDate())
                        .orderSerial(orderProduct.getOrders().getOrdersSerial())
                        .build();
                responses.add(response);
            }
        }

        for(Refund refund : refunds){
            Optional<OrderProduct> orderProduct
                    = orderProductRepository.findById(refund.getOrderProductId());
            OrderRefundResponse response = OrderRefundResponse.builder()
                    .cancelDetail(refund.getRefundContent())
                    .productPrice(orderProduct.get().getOrderProductPrice())
                    .productQty(orderProduct.get().getOrderProductQty())
                    .productTotalPrice(
                            orderProduct.get().getOrderProductPrice() * orderProduct.get().getOrderProductQty())
                    .productStatus(orderProduct.get().getOrderProductStatus())
                    .productName(orderProduct.get().getOrderProductName())
                    .productImage(orderProduct.get().getOrderProductMainImg())
                    .orderDate(orderProduct.get().getOrderProductDate())
                    .orderSerial(orderProduct.get().getOrders().getOrdersSerial())
                    .build();
            responses.add(response);
        }

        OrderRefundResultResponse resultResponse = new OrderRefundResultResponse();

        responses.sort((o1, o2) -> {
            int result = o2.getOrderDate().compareTo(o1.getOrderDate());
            return result;
        });

        int startIndex = orderUserFindDto.getPageNumber()*pageContentNumber;

        int size = responses.size();

        if(size<startIndex+pageContentNumber){
            resultResponse.setResponses(responses.subList(startIndex,size));
            resultResponse.setCurrentPageNum(orderUserFindDto.getPageNumber());
            if(size%pageContentNumber!=0){
                resultResponse.setTotalPageNum((size/pageContentNumber)+1);
            }
            else{
                resultResponse.setTotalPageNum(size/pageContentNumber);
            }
            return resultResponse;
        }

        resultResponse.setResponses(responses.subList(startIndex,startIndex+pageContentNumber));
        resultResponse.setCurrentPageNum(orderUserFindDto.getPageNumber());
        if(size%pageContentNumber!=0){
            resultResponse.setTotalPageNum((size/pageContentNumber)+1);
        }
        else{
            resultResponse.setTotalPageNum(size/pageContentNumber);
        }
        return resultResponse;
    }

    /**
     * 셀러 반품 상세 내역 조회
     * @param orderProductId
     * @return
     */
    public RefundDetailResponse findRefundDetail(Long orderProductId){
        CircuitBreaker userCircuitbreaker = circuitbreakerFactory.create("userCircuitbreaker");

        Refund refund = refundRepository.findByOrderProductId(orderProductId);
        Optional<OrderProduct> orderProduct = orderProductRepository.findById(orderProductId);

        UserVo user
                = userCircuitbreaker.run(
                        ()->userServiceClient.findByUserId(orderProduct.get().getOrders().getUserId()),
                throwable -> new UserVo());
        //UserVo user = userServiceClient.findByUserId(orderProduct.get().getOrders().getUserId());
        RefundDetailResponse response = RefundDetailResponse.builder()
                .cancelDetail(refund.getRefundContent())
                .productName(orderProduct.get().getOrderProductName())
                .productPrice(orderProduct.get().getOrderProductPrice())
                .productQty(orderProduct.get().getOrderProductQty())
                .productStatus(orderProduct.get().getOrderProductStatus())
                .refundImage(refund.getRefundImage())
                .productImage(orderProduct.get().getOrderProductMainImg())
                .userAddress(user.getUserAddress())
                .userPhone(user.getUserPhone())
                .userName(user.getUserName())
                .productTotalPrice(
                        orderProduct.get().getOrderProductPrice() *
                                orderProduct.get().getOrderProductQty())
                .build();
        return response;
    }

    /**
     * 반품 처리해주는 메서드
     * @param orderProductId
     * @return
     */
    public Boolean conformRefund(Long orderProductId){
        Optional<OrderProduct> orderProduct = orderProductRepository.findById(orderProductId);
        orderProduct.get().setOrderProductStatus("refundCompleted"); // 반품 확정
        if(orderProduct.get().getOrderProductStatus().equals("refundCompleted")){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 배송 시작을 해주는 메서드
     * @param orderDeliveryDto
     * @return
     */
    public Boolean deliveryStart(OrderDeliveryDto orderDeliveryDto){
        Orders orders = orderRepository.findByOrdersSerial(orderDeliveryDto.getOrderSerial());
        orders.setOrdersDeliveryCompany(orderDeliveryDto.getOrderDeliveryCompany());
        orders.setOrdersDeliveryDate(dateUtils.transDate(env.getProperty("dateutils.format")));
        orders.setOrdersDeliveryWaybillNumber(orderDeliveryDto.getOrderDeliveryWaybillNumber());
        orders.setOrdersStatus("deliveryProgress");

        List<OrderProduct> orderProducts = orderProductRepository.findByOrders(orders);
        for(OrderProduct orderProduct : orderProducts){
            if(!orderProduct.getOrderProductStatus().equals("canceled")) {
                orderProduct.setOrderProductStatus("deliveryProgress");
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 배송을 완료처리해주는 메서드
     * @param orderSerial
     * @return
     */
    public Boolean deliveryConform(String orderSerial){
        Orders orders = orderRepository.findByOrdersSerial(orderSerial);
        orders.setOrdersStatus("deliveryCompleted");

        List<OrderProduct> orderProducts = orderProductRepository.findByOrders(orders);
        for(OrderProduct orderProduct : orderProducts){
            if(orderProduct.getOrderProductStatus().equals("deliveryProgress")) {
                orderProduct.setOrderProductStatus("deliveryCompleted");
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public OrdersConditionResponse findOrdersCondition(Long sellerId) {
        CircuitBreaker productCircuitbreaker = circuitbreakerFactory.create("productCircuitbreaker");

        Integer beforeDelivery = orderProductRepository.countBySellerIdAndOrderProductStatus(sellerId,"activated");
        Integer requestRefund = orderProductRepository.countBySellerIdAndOrderProductStatus(sellerId,"refundRequest");
        Integer cancelOrders = orderProductRepository.countBySellerIdAndOrderProductStatus(sellerId,"canceled");
        Integer delivering = orderProductRepository.countBySellerIdAndOrderProductStatus(sellerId,"deliveryProgress");
        Integer deliverComplete = orderProductRepository.countBySellerIdAndOrderProductStatus(sellerId,"deliveryCompleted");
        List<ProductVo> notSelling
                = productCircuitbreaker.run(
                        ()->productServiceClient.findNotSellingProduct(sellerId),
                throwable -> new ArrayList<>());
        //List<ProductVo> notSelling = productServiceClient.findNotSellingProduct(sellerId);
        List<ProductVo> selling
                = productCircuitbreaker.run(
                ()->productServiceClient.findSellingProduct(sellerId),
                throwable -> new ArrayList<>());
        //List<ProductVo> selling = productServiceClient.findSellingProduct(sellerId);
        List<ProductQnaVo> beforeAnswer
                = productCircuitbreaker.run(
                ()->productServiceClient.findBeforeAnswerProductQna(sellerId),
                throwable -> new ArrayList<>());
        //List<ProductQnaVo> beforeAnswer = productServiceClient.findBeforeAnswerProductQna(sellerId);

        OrdersConditionResponse ordersConditionResponse = OrdersConditionResponse.builder()
                .beforeDelivery(beforeDelivery)
                .requestRefund(requestRefund)
                .cancelOrders(cancelOrders)
                .delivering(delivering)
                .notSelling(notSelling.size())
                .beforeAnswer(beforeAnswer.size())
                .sellingProducts(selling.size())
                .deliverCompletes(deliverComplete)
                .build();

        return ordersConditionResponse;
    }

    /**
     *  유효성 검사하는 메서드
     * @param orderId
     * @return true: 중복 안된 메시지 / false : 중복된 메시지
     */
    @Override
    public Boolean isAlreadyProcessedOrderId(Long orderId) {
        Optional<Orders> orders = orderRepository.findById(orderId);
        if(orders.isEmpty()){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
