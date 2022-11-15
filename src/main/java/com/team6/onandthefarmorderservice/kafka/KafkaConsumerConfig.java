package com.team6.onandthefarmorderservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

@EnableKafka
@Configuration
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaTemplate<String, String> template;

    @Value("${kafka.url}")
    private String kafkaUrl;

    @Bean
    public ConsumerFactory<String,String> consumerFactory(){
        Map<String,Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,kafkaUrl);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,"order-group");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,false);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"latest");
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,1);
        properties.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,600000);
        return new DefaultKafkaConsumerFactory<>(properties);
    }
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String,String> kafkaListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String,String> kafkaListenerContainerFactory
                = new ConcurrentKafkaListenerContainerFactory<>();
        kafkaListenerContainerFactory.setConsumerFactory(consumerFactory());
        kafkaListenerContainerFactory.setCommonErrorHandler(kafkaListenerErrorHandler());
        kafkaListenerContainerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        //kafkaListenerContainerFactory.getContainerProperties().setPollTimeout(3000);
        //kafkaListenerContainerFactory.getContainerProperties().setSyncCommits(true);
        return kafkaListenerContainerFactory;
    }

    /**
     * 몇번의 재시도를 할 것인지, 재시도 끝에도 처리되지 않는 메시지를 DLQ로 보내는 메서드
     * @return
     */
    @Bean
    public CommonErrorHandler kafkaListenerErrorHandler() {
        DefaultErrorHandler defaultErrorHandler = new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(template, DEAD_TOPIC_DESTINATION_RESOLVER),
                new FixedBackOff(1000, 5)); // 1초간격으로 최대 3번 시도한다는 의미

        defaultErrorHandler.setCommitRecovered(true); // DLQ로 보내지면(리커버리 되었다는의미) 오프셋을 커밋한다는 의미
        defaultErrorHandler.setAckAfterHandle(true);
        defaultErrorHandler.setResetStateOnRecoveryFailure(false);

        return defaultErrorHandler;
    }

    /**
     * DLQ를 지정해 보내주는 익명 함수
     */
    public static final BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition>
            DEAD_TOPIC_DESTINATION_RESOLVER = (cr, e) -> {
        log.error("[Send to dead letter topic]: {} - [Exception message] : {}" , cr.topic(), e);
        return new TopicPartition("dlt-" + cr.topic(), cr.partition());
    };
}