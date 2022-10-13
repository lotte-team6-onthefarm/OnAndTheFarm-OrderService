package com.team6.onandthefarmorderservice.kafka.dto;

import com.team6.onandthefarmorderservice.kafka.payload.OrderProductPayload;
import com.team6.onandthefarmorderservice.kafka.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class KafkaOrderProductDto implements Serializable {
    private Schema schema;

    private OrderProductPayload payload;
}
