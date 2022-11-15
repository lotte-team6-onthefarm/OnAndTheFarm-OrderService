package com.team6.onandthefarmorderservice.kafka.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Payload {
    private Long orders_id;

    private String orders_address;

    private String orders_date;

    private String orders_delivery_company;

    private String orders_delivery_date;

    private String orders_delivery_waybill_number;

    private String orders_phone;

    private String orders_recipient_name;

    private String orders_request;

    //private String ordersDeliveryStatus;

    private Long orders_seller_id;

    private String orders_serial;

    private String orders_status;

    private Integer orders_total_price;

    private Long user_id;
}
