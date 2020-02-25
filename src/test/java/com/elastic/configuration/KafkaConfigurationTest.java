package com.elastic.configuration;

import com.elastic.plugins.kafka.consumer.ReceiveConfig;
import com.elastic.plugins.kafka.producer.SenderConfig;
import com.elastic.service.NoiseDataManager;
import com.google.gson.internal.LinkedTreeMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2020-01-13 오후 3:06
 **/
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class KafkaConfigurationTest {
    @Autowired
    private NoiseDataManager noiseDataManager;
    @Autowired
    private KafkaProperties kafkaProperties;
    @Autowired
    private SenderConfig senderConfig;
    @Autowired
    private ReceiveConfig receiveConfig;

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
    }

    /**
     * Local Noise Data -> Kafka Producer 멀티스레딩 구현
     * int fixedThreadCount 로 스레드 수 조정
     **/
    @Test
    public void localToKafkaProducerTest() {
        int fixedThreadCount = 10;
        String noiseData = getNoiseData();
        // TODO: 2020-01-13 멀티스레딩 구현
        ExecutorService es = Executors.newFixedThreadPool(fixedThreadCount);
        List<Callable<Void>> taskCollectList = Lists.newArrayList();

        try {
            for (int i = 0; i < fixedThreadCount; i++) {
                taskCollectList.add(() -> {
                    senderConfig.highLevelSender(noiseData);
                    return null;
                });
            }
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            es.invokeAll(taskCollectList);
            es.shutdown();
            log.info("##### Shutdown Thread-Pool");

            stopWatch.stop();
            log.info("################################");
            log.info("##### Total Producer Thread Execution Time = {} ms / Thread Count is {}", stopWatch.getTotalTimeMillis(), fixedThreadCount);
            log.info("##### Avg Producer Thread Execution Time = {} ms", stopWatch.getTotalTimeMillis() / (kafkaProperties.getDoccount()*fixedThreadCount));
        } catch (InterruptedException e) {
            log.error("", e);
        }
    }

    /**
     * noise Data -> File(.txt) 적재 로직
     **/
    @Test
    public void saveToFile(){
        String noiseData= getNoiseData();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("noiseData.txt"));
            writer.write(noiseData);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * getRandomAccessData() : 소음데이터 파일의 랜덤한 위치에서 10000hz 1분 데이터(60만 row)를 추출하여
     * @return String
     **/
    private String getNoiseData() {
        LinkedTreeMap<String, String> noiseMap = null;
        String mapKey = null;
        try {
            noiseMap = noiseDataManager.getRandomAccessData(file, lineNum, bundleNum).get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert noiseMap != null;
        for (String key : noiseMap.keySet())
            mapKey = key;
        return noiseMap.get(mapKey);
    }
}
