package com.elastic.config;

import com.elastic.plugins.kafka.consumer.ReceiveConfig;
import com.elastic.service.NoiseDataManager;
import com.google.gson.internal.LinkedTreeMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.StopWatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2020-01-13 오전 11:39
 **/
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class EsHighConfigurationTest {
    @Autowired
    private EsProperties esProperties;
    @Autowired
    private NoiseDataManager noiseDataManager;
    @Autowired
    private ReceiveConfig receiveConfig;
    @Autowired
    private EsHighConfiguration esHighConfiguration;

    private RestHighLevelClient restHighLevelClient;

    private int lineNum;
    private int bundleNum;
    private String indexName;
    private String file;

    @Before
    public void init() {
        lineNum = 600000;
        bundleNum = 600000;
        indexName = "webtest";
        file = "D:\\\\99.TEMP\\\\noise\\\\r_10min.csv";
        restHighLevelClient = esHighConfiguration.restHighLevelClient();
    }

    @Test
    public void esConnectionCheck() {
        try {
            boolean ping = restHighLevelClient.ping(RequestOptions.DEFAULT);
            System.out.println(ping);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 싱글스레드로 로컬 데이터 -> ES 적재 로직
     **/
    @Test
    public void localToEsSave() {
        IndexRequest indexRequest = getNoiseDataTest();
        indexNoiseDataTest(indexRequest);
    }

    /**
     * kafka 데이터 Consumer로 읽어와서 -> ES 적재 로직
     * ES적재시 Bulk 기능 사용 안함
     * MultiThreading 사용 안함
     **/
    @Test
    public void kafkaConsumerTest() {
        // TODO: 2020-01-15 해당 로직 멀티스레딩 구현,
//        while(true){
        int size = 0;
        List<Map<String, String>> consumer = receiveConfig.highLevelConsumer();
        log.info("##### Consumer List is {}", consumer.size());
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            for (Map<String, String> consumerMap : consumer) {
                IndexRequest indexRequest = new IndexRequest()
                        .index(indexName)
                        .source(consumerMap);

                IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
                if (indexResponse.getShardInfo().getFailed() > 0) {
                    log.error("##### Index response is failed {}", indexName);
                }
                size = consumer.size();
            }
            stopWatch.stop();
            log.info("################################");
            log.info("##### Total Execution Time = {} ms", stopWatch.totalTime().getMillis());
            if (size != 0)
                log.info("##### Avg Execution Time per case = {} ms", stopWatch.totalTime().getMillis() / size);
//                Thread.sleep(60000);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        }
    }

    /**
     * MultiThreading 사용
     * kafka 데이터 Consumer로 읽어와서 -> ES 적재 로직
     * ES적재시 Bulk 기능 사용 안함
     **/
    @Test
    public void kafkaConsumerThread() {
        // TODO: 2020-01-16 멀티스레딩 구현 -> Kafka Consumer Performance Up
        final int fixedThreadCount = 30;
        int size = 0;
        boolean task = false;

        ExecutorService es = Executors.newFixedThreadPool(fixedThreadCount);
        List<Callable<List<Map<String, String>>>> taskList = Lists.newArrayList();
        for (int threadCount = 0; threadCount < fixedThreadCount; threadCount++) {
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
                log.info("################################");
                log.info("##### list size is {}", entry.size());

                List<Callable<Void>> taskCollectList = Lists.newArrayList();
                for (Map<String, String> entryMap : entry) {
                    taskCollectList.add(() -> {
                        IndexRequest indexRequest = new IndexRequest()
                                .index(indexName)
                                .source(entryMap);
                        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
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

    // TODO: 2020-01-22 Fork Join Pool 구현
    public static class SaveTask extends RecursiveTask<List<Map<String, String>>> {
        @Override
        protected List<Map<String, String>> compute() {
            return null;
        }
    }

    /**
     * MultiThreading 적용
     * getNoiseDataToListTest() : 10000hz 1분데이터, 70묶음을 하나의 List로 받아와서
     * ES Bulk 기능으로 데이터 적재
     **/
    // TODO: 2020-01-16 Consumer하고 데이터 ES로 적재시 실패했을 때 kafka 롤백하는 기능 추가
    @Test
    public void kafkaConsumerBulkTest() {
        // TODO: 2020-01-16 멀티스레딩 구현 -> Kafka Consumer Performance Up
        final int fixedThreadCount = 1;
        ExecutorService es = Executors.newFixedThreadPool(fixedThreadCount);
        List<Callable<List<Map<String, String>>>> taskList = Lists.newArrayList();
        for (int threadCount = 0; threadCount < fixedThreadCount; threadCount++) {
            taskList.add(() -> {
                List<Map<String, String>> consumer = receiveConfig.highLevelConsumer();
                log.info("##### consumer size is {}", consumer.size());
                return consumer;
            });
        }
        try {
            List<Future<List<Map<String, String>>>> futures = es.invokeAll(taskList);
            for (Future<List<Map<String, String>>> future : futures) {
                List<Map<String, String>> entry = future.get();
                log.info("##### list size is {}", entry.size());

                // TODO: 2020-01-16 IndexRequest의 크기를 줄여야한다.
                BulkRequest request = new BulkRequest();
                Stream<IndexRequest> indexRequestStream = entry.stream()
                        .map(stream -> new IndexRequest()
                                .index(indexName)
                                .source(stream));

                IndexRequest[] indexRequests = indexRequestStream.toArray(IndexRequest[]::new);
                request.add(indexRequests);

                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                // request의 크기가 50MB를 안넘도록 해야한다.
                // request의 크기가 너무 크면 ES ERROR 발생
                BulkResponse bulkItemResponses = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
                log.info("##### Bulk Success. {}", (Object) bulkItemResponses.getItems());
                stopWatch.stop();
                log.info("################################");
                log.info("##### Total Execution Time = {} ms", stopWatch.totalTime().getMillis());
                int size = 70;
                if (size != 0)
                    log.info("##### Avg Execution Time per case = {} ms", stopWatch.totalTime().getMillis() / size);
            }

        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
        }
    }

    public void kafkaConsumerWithFuture() {

    }

    /**
     * getRandomAccessData() : 소음데이터 파일의 랜덤한 위치에서 10000hz 1분 데이터(60만 row)를 추출하여
     *
     * @return IndexRequest
     **/
    public IndexRequest getNoiseDataTest() {
        final LinkedTreeMap<String, String> noiseMap;
        try {
            noiseMap = noiseDataManager.getRandomAccessData(file, lineNum, bundleNum).get(0);
            return new IndexRequest()
                    .index(indexName)
                    .source(noiseMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ES Bulk Test용
     * 10000hz 1분 데이터로 70짜리 list 작성
     *
     * @return List<LinkedTreeMap < String, String>>
     **/
    public List<LinkedTreeMap<String, String>> getNoiseDataToListTest() {
        final LinkedTreeMap<String, String> noiseMap;
        final List<LinkedTreeMap<String, String>> noiseList = new ArrayList<>();
        try {
            noiseMap = noiseDataManager.getRandomAccessData(file, lineNum, bundleNum).get(0);
            for (int i = 0; i < 70; i++) {
                noiseList.add(noiseMap);
            }
            return noiseList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * IndexRequest를 RestHighLevelClient를 이용하여 ES로 적재
     **/
    public void indexNoiseDataTest(IndexRequest indexRequest) {
        try {
            int size = 0;

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            for (int i = 0; i < 70; i++) {
                IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
                if (indexResponse.getShardInfo().getFailed() > 0) {
                    log.error("##### Index response is failed {}", indexName);
                }

                if ((i + 1) % 10 == 0) {
                    log.info("###### current es save count {}", i + 1);
                    size = i + 1;
                }
            }
            stopWatch.stop();

            log.info("################################");
            log.info("##### Total Execution Time = {} ms", stopWatch.totalTime().getMillis());
            log.info("##### Avg Execution Time per case = {} ms", stopWatch.totalTime().getMillis() / size);
            log.info("##### Success get File is {}", file);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void methTest() {
        int a = 24;
        int b = 0;

        if ((a / 35) > 0) {
            b = a / 35;
        } else {
            b = 1;
        }
        log.info(String.valueOf(b));
    }
}