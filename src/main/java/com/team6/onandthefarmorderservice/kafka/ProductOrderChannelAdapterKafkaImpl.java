package com.team6.onandthefarmorderservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProductOrderChannelAdapterKafkaImpl implements ProductOrderChannelAdapter {
    private final String TOPIC = "dlt-order";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void producer(String message) {
        this.kafkaTemplate.send(TOPIC, message);
    }
}
