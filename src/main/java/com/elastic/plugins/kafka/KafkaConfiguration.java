package com.elastic.plugins.kafka;

import com.elastic.config.EsHighConfiguration;
import com.elastic.config.EsProperties;
import com.elastic.plugins.kafka.consumer.ReceiveConfig;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2020-01-15 오전 9:49
 **/
@Slf4j
@Component
public class KafkaConfiguration {
    @Autowired
    private EsProperties esProperties;
    @Autowired
    private EsHighConfiguration esHighConfiguration;
    @Autowired
    private ReceiveConfig receiveConfig;

    @Scheduled
    public void kafkaConsumer() {
        // TODO: 2020-01-13 kafka data -> es save 로직 구현
        // TODO: 2020-01-14 해당 로직 멀티스레딩 구현
        int size = 0;
        List<Map<String, String>> consumer = receiveConfig.highLevelConsumer();
        log.info("##### Consumer List is {}",consumer.size());
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            for (Map<String, String> consumerMap : consumer) {
                for (String key : consumerMap.keySet()) {
//                    log.info(key + " # map length is {}", consumerMap.get(key).length());
                    IndexRequest indexRequest = new IndexRequest()
                            .index(esProperties.getIndexname())
                            .source(consumerMap);

                    IndexResponse indexResponse = esHighConfiguration.restHighLevelClient().index(indexRequest, RequestOptions.DEFAULT);
                    if (indexResponse.getShardInfo().getFailed() > 0) {
                        log.error("##### Index response is failed {}", esProperties.getIndexname());
                    }
                }
                size = consumer.size();
            }
            stopWatch.stop();
            log.info("################################");
            log.info("##### Total Execution Time = {} ms", stopWatch.totalTime().getMillis());
            log.info("##### Avg Execution Time per case = {} ms", stopWatch.totalTime().getMillis() / size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
