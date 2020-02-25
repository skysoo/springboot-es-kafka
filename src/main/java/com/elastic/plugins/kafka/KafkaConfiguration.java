package com.elastic.plugins.kafka;

import com.elastic.configuration.CommonProperties;
import com.elastic.configuration.EsHighConfiguration;
import com.elastic.configuration.EsProperties;
import com.elastic.plugins.kafka.consumer.ReceiveConfig;
import com.elastic.plugins.kafka.producer.SenderConfig;
import com.elastic.service.NoiseDataManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
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
import java.util.concurrent.*;

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
    private CommonProperties commonProperties;
    @Autowired
    private EsHighConfiguration esHighConfiguration;
    @Autowired
    private ReceiveConfig receiveConfig;
    @Autowired
    private SenderConfig senderConfig;
    @Autowired
    private NoiseDataManager noiseDataManager;



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

    @Scheduled
    public void kafkaConsumerThread(int consumerCount,String indexName){
        int size=0;
        final int consumerThreadCount = commonProperties.getConsumerthreadcount();
        if (consumerCount == 0) consumerCount = consumerThreadCount;
//        final String indexName = esProperties.getIndexname();

        ExecutorService es = Executors.newFixedThreadPool(consumerCount);
        List<Callable<List<Map<String,String>>>> taskList = Lists.newArrayList();
        for (int threadCount = 0; threadCount < consumerCount; threadCount++) {
            taskList.add(() -> {
                List<Map<String, String>> consumer = receiveConfig.highLevelConsumer();
                 log.info("##### Consumer size is {}", consumer.size());
                return consumer;
            });
        }
        try {
            StopWatch stopWatch1 = new StopWatch();
            stopWatch1.start();

            List<Future<List<Map<String, String>>>> futures = es.invokeAll(taskList);

            stopWatch1.stop();
            log.info("################################");
            log.info("##### Total Consumer Execution Time = {} ms", stopWatch1.totalTime().getMillis());

            StopWatch stopWatch2 = new StopWatch();
            stopWatch2.start();
            for (Future<List<Map<String, String>>> future : futures) {
                List<Map<String, String>> entry = future.get();
                if (entry==null) {log.error("entry is null.");} else {}
                log.info("################################");
                log.info("##### list size is {}", entry.size());

                List<Callable<Void>> taskCollectList = Lists.newArrayList();
                for (Map<String, String> entryMap : entry) {
                    taskCollectList.add(() -> {
                        IndexRequest indexRequest = new IndexRequest()
                                .index(indexName)
                                .source(entryMap);
                        IndexResponse indexResponse = esHighConfiguration.restHighLevelClient()
                                .index(indexRequest, RequestOptions.DEFAULT);
                        if (indexResponse.getShardInfo().getFailed() > 0) {
                            log.error("##### Index response is failed {}", indexName);
                        }
                        return null;
                    });
                    size++;
                }
                try {
                    StopWatch stopWatch = new StopWatch();
                    stopWatch.start();

                    es.invokeAll(taskCollectList);

                    size = entry.size();
                    stopWatch.stop();
                    log.info("##### Total ES Execution Time = {} ms", stopWatch.totalTime().getMillis());
                    if (size != 0)
                        log.info("##### Avg ES Execution Time per case = {} ms, count = {}", stopWatch.totalTime().getMillis() / size, size);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            stopWatch2.stop();
            log.info("################################");
            log.info("##### Total ES Thread Execution Time = {} ms", stopWatch2.totalTime().getMillis());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Scheduled
    public void kafkaProducerThread(int producerCount){
        final int producerThreadCount = commonProperties.getProducerthreadcount();
        if(producerCount==0) producerCount = producerThreadCount;

        String singleNoiseData = noiseDataManager.getSingleNoiseData(commonProperties.getFilepath());
        // TODO: 2020-01-29 file path의 경로값을 받았을 때 실제 file인지 확인하는 validation 필요

        ExecutorService es = Executors.newFixedThreadPool(producerCount);
        List<Callable<Void>> taskCollectList = Lists.newArrayList();

        try {
            for (int i = 0; i < producerCount; i++) {
                taskCollectList.add(() -> {
                    senderConfig.highLevelSender(singleNoiseData);
                    return null;
                });
            }
            org.springframework.util.StopWatch stopWatch = new org.springframework.util.StopWatch();
            stopWatch.start();

            es.invokeAll(taskCollectList);
            es.shutdown();
            log.info("##### Shutdown Thread-Pool");

            stopWatch.stop();
            log.info("################################");
            log.info("##### Total Producer Thread Execution Time = {} ms / Thread Count is {}", stopWatch.getTotalTimeMillis(), producerCount);
            log.info("##### Avg Producer Thread Execution Time = {} ms", stopWatch.getTotalTimeMillis() / producerCount);
        } catch (InterruptedException e) {
            log.error("", e);
        }
    }



}
