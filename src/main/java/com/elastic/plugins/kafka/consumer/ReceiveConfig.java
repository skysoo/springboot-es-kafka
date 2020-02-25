package com.elastic.plugins.kafka.consumer;

import com.elastic.configuration.KafkaProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.time.Duration;
import java.util.*;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2020-01-13 오후 2:08
 **/
@Slf4j
@EnableKafka
@Configuration
public class ReceiveConfig {
    private final KafkaProperties kafkaProperties;

    public ReceiveConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public Map<String, Object> consumerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getServers());
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getConsumergroup());
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG,180000);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,150000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG,60000);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,kafkaProperties.getPollrecords());
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,kafkaProperties.getPollinterval());
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 1024 * 1024 * 50);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 1024 * 1024 * 50);
        return props;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfig());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    public List<Map<String, String>> highLevelConsumer(){
        final Consumer<String,String> consumer = consumerFactory().createConsumer();
        final Map<String,String> consumerMap = new HashMap<>();
        final List<Map<String,String>> consumerList = new ArrayList<>();

        consumer.subscribe(Arrays.asList(kafkaProperties.getTopicname()));

        Integer giveUp = kafkaProperties.getGiveup();
        Integer duration = kafkaProperties.getPollduration();
        log.info("giveUp : {}, duration : {}",giveUp,duration);

        int noRecordsCount = 0;
        while (true){
            final ConsumerRecords<String,String> consumerRecords = consumer.poll(Duration.ofSeconds(duration));

            if (consumerRecords.count() == 0){
                noRecordsCount++;
                if (noRecordsCount%10==0) log.info("##### No Records Count is = {}",noRecordsCount);
                if (noRecordsCount>giveUp) break;
                else continue;
            }else {
                log.info("##### Consumer Records Count is {}",consumerRecords.count());
            }
            consumerRecords.forEach(record -> {
                consumerMap.put(String.valueOf(record.value().hashCode()),record.value());
                consumerList.add(consumerMap);
                log.info("Topic : {}, Partition : {}, Offset : {}, Key : {}", record.topic(), record.partition(), record.offset(), record.key());
            });
            consumer.commitAsync();
        }
        consumer.close();
        return consumerList;
    }
}
