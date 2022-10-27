package com.team6.onandthefarmorderservice.kafka;

import org.springframework.kafka.support.Acknowledgment;

public interface ProductOrderChannelAdapter {
    void producer(String message);
}
