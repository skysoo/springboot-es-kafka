package com.elastic.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2020-01-13 오후 2:10
 **/
@Component
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {
    // KAFKA Broker 서버 주소
    private String servers;
    // KAFKA TOPIC 이름
    private String topicname;
    // ES에 저장할 문서 갯수
    private int doccount;
    // KAFKA CONSUMER GROUP 이름
    private String consumergroup;
    // POLLING할 records 갯수
    private Integer pollrecords;
    // POLLING 타임
    private Integer pollinterval;
    // giveup 숫자만큼 while문을 돌고 그동안 데이터가 안들어오면 polling 중단
    private Integer giveup;
    // POLLING 범위 지정
    private Integer pollduration;

    @Override
    public String toString() {
        return "KafkaProperties{" +
                "servers='" + servers + '\'' +
                ", topicname='" + topicname + '\'' +
                ", doccount=" + doccount +
                ", consumergroup='" + consumergroup + '\'' +
                ", pollrecords=" + pollrecords +
                ", pollinterval=" + pollinterval +
                ", giveup=" + giveup +
                ", pollduration=" + pollduration +
                '}';
    }

    public Integer getPollrecords() {
        return pollrecords;
    }

    public void setPollrecords(Integer pollrecords) {
        this.pollrecords = pollrecords;
    }

    public Integer getPollinterval() {
        return pollinterval;
    }

    public void setPollinterval(Integer pollinterval) {
        this.pollinterval = pollinterval;
    }

    public Integer getGiveup() {
        return giveup;
    }

    public void setGiveup(Integer giveup) {
        this.giveup = giveup;
    }

    public Integer getPollduration() {
        return pollduration;
    }

    public void setPollduration(Integer pollduration) {
        this.pollduration = pollduration;
    }

    public String getConsumergroup() {
        return consumergroup;
    }

    public void setConsumergroup(String consumergroup) {
        this.consumergroup = consumergroup;
    }

    public int getDoccount() {
        return doccount;
    }

    public void setDoccount(int doccount) {
        this.doccount = doccount;
    }

    public String getServers() {
        return servers;
    }

    public void setServers(String servers) {
        this.servers = servers;
    }

    public String getTopicname() {
        return topicname;
    }

    public void setTopicname(String topicname) {
        this.topicname = topicname;
    }
}
