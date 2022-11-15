package com.team6.onandthefarmorderservice.kafka.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Field {
    private String type;

    private boolean optional;

    private String field;
}
