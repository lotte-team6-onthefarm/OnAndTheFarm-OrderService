package com.team6.onandthefarmorderservice.kafka.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderProductPayload {
    private Long order_product_id;
    private String order_product_date;
    private String order_product_main_img;
    private String order_product_name;
    private Integer order_product_price;
    private Integer order_product_qty;
    private String order_product_status;
    private Long product_id;
    private Long seller_id;
    private Long orders_id;
}
