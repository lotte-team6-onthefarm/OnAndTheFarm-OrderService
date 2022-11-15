package com.team6.onandthefarmorderservice.kafka;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.team6.onandthefarmorderservice.kafka.vo.Payload;
import com.team6.onandthefarmorderservice.repository.OrderRepository;
import com.team6.onandthefarmorderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderChannelAdapterKafkaImpl implements OrderChannelAdapter {
    private final String TOPIC = "orders_sink";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void producer(String message) {
        this.kafkaTemplate.send(TOPIC, message);
    }
}
