package com.elastic.plugins.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2020-01-29 오후 6:44
 **/
@Slf4j
@RestController
@RequestMapping("/kafka")
public class KafkaController {
    @Autowired
    private KafkaConfiguration kafkaConfiguration;

    @GetMapping("/get_kafka_consumer")
    public void getKafkaConsumer(@RequestParam int consumerCount, @RequestParam String indexName){
        kafkaConfiguration.kafkaConsumerThread(consumerCount,indexName);
    }

    @PostMapping("/post_kafka_producer_to_es")
    public void postKafkaProducerToEs(@RequestParam int producerCount){
        kafkaConfiguration.kafkaProducerThread(producerCount);
    }

}
