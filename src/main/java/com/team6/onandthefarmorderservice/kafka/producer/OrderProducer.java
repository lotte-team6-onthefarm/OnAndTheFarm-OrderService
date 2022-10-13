package com.team6.onandthefarmorderservice.kafka.producer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team6.onandthefarmorderservice.entity.OrderProduct;
import com.team6.onandthefarmorderservice.kafka.Field;
import com.team6.onandthefarmorderservice.kafka.Schema;
import com.team6.onandthefarmorderservice.kafka.dto.KafkaOrderProductDto;
import com.team6.onandthefarmorderservice.kafka.payload.OrderProductPayload;
import com.team6.onandthefarmorderservice.repository.OrderProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class OrderProducer {
    private KafkaTemplate<String,String> kafkaTemplate;

    private OrderProductRepository orderProductRepository;

    List<Field> fields = Arrays.asList(new Field("int64",false,"order_product_id"),
            new Field("string",true,"order_product_date"),
            new Field("string",true,"order_product_main_img"),
            new Field("string",true,"order_product_name"),
            new Field("int32",true,"order_product_price"),
            new Field("int32",true,"order_product_qty"),
            new Field("string",true,"order_product_status"),
            new Field("int64",true,"product_id"),
            new Field("int64",true,"seller_id"),
            new Field("int64",false,"orders_id"));
    Schema schema = Schema.builder()
            .type("struct")
            .fields(fields)
            .optional(false)
            .name("order_product")
            .build();
    @Autowired
    public OrderProducer(KafkaTemplate<String, String> kafkaTemplate,
                         OrderProductRepository orderProductRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderProductRepository=orderProductRepository;
    }

    public void refundSend(String topic, Long orderProductId){
        OrderProduct orderProduct = orderProductRepository.findById(orderProductId).get();
        OrderProductPayload payload = OrderProductPayload.builder()
                .order_product_id(orderProductId)
                .order_product_date(orderProduct.getOrderProductDate())
                .order_product_main_img(orderProduct.getOrderProductMainImg())
                .order_product_name(orderProduct.getOrderProductName())
                .order_product_price(orderProduct.getOrderProductPrice())
                .order_product_qty(orderProduct.getOrderProductQty())
                .order_product_status("refundCompleted")
                .product_id(orderProduct.getProductId())
                .seller_id(orderProduct.getSellerId())
                .orders_id(orderProduct.getOrders().getOrdersId())
                .build();
        KafkaOrderProductDto kafkaOrderDto = new KafkaOrderProductDto(schema, payload);

        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try{
            jsonInString = mapper.writeValueAsString(kafkaOrderDto);
        }catch(JsonProcessingException ex){
            ex.printStackTrace();
        }

        kafkaTemplate.send(topic,jsonInString);

    }
}
