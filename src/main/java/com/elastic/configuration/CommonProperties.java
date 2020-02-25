package com.elastic.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2020-01-29 오후 6:25
 **/
@Component
@ConfigurationProperties(prefix = "common")
public class CommonProperties {
    // 작업 thread 수
    private int threadcount;
    // KAFKA Producer 작업 thread 수
    private int producerthreadcount;
    // KAFKA Consumer 작업 thread 수
    private int consumerthreadcount;
    // 데이터 파일 경로
    private String filepath;

    @Override
    public String toString() {
        return "CommonProperties{" +
                "threadcount=" + threadcount +
                ", producerthreadcount=" + producerthreadcount +
                ", consumerthreadcount=" + consumerthreadcount +
                ", filepath='" + filepath + '\'' +
                '}';
    }

    public int getProducerthreadcount() {
        return producerthreadcount;
    }

    public void setProducerthreadcount(int producerthreadcount) {
        this.producerthreadcount = producerthreadcount;
    }

    public int getConsumerthreadcount() {
        return consumerthreadcount;
    }

    public void setConsumerthreadcount(int consumerthreadcount) {
        this.consumerthreadcount = consumerthreadcount;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public int getThreadcount() {
        return threadcount;
    }

    public void setThreadcount(int threadcount) {
        this.threadcount = threadcount;
    }
}
