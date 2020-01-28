package com.elastic.plugins.kafka.producer;

import com.elastic.config.KafkaProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.StopWatch;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2020-01-13 오후 2:15
 **/
@Slf4j
@Configuration
public class SenderConfig {
    private final KafkaProperties kafkaProperties;

    public SenderConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public Map<String, Object> producerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getServers());
        props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 308002330);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");
//        props.put(ProducerConfig.ACKS_CONFIG,1);
        return props;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    public void send(String msg) {
        log.info("sending to topic = {}", kafkaProperties.getTopicname());
        int size = 0;
        final int docCount = kafkaProperties.getDoccount();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 0; i < docCount; i++) {
            ListenableFuture<SendResult<String, String>> send = kafkaTemplate().send(kafkaProperties.getTopicname(), msg);
            if ((i + 1) % 10 == 0) {
                log.info("###### current kafka save count {}", i + 1);
            }
            size++;
            send.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onFailure(Throwable throwable) {
                    log.warn("Unable to send message length =[" + msg.length() + "] due to : " + throwable.getMessage());
                }
                @Override
                public void onSuccess(SendResult<String, String> result) {
                    log.debug("Sent message length =[" + msg.length() + "] with offset=[" + result.getRecordMetadata().offset() + "]");
                }
            });
        }
        stopWatch.stop();
        log.info("################################");
        log.info("##### Total Execution Time = {} ms", stopWatch.getTotalTimeMillis());
        log.info("##### Avg Execution Time per case = {} ms, count = {}", stopWatch.getTotalTimeMillis() / size,size);
    }
}