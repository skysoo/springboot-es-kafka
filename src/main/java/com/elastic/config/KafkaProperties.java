package com.elastic.config;

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
    private String servers;
    private String topicname;
    private int doccount;
    private String consumergroup;

    @Override
    public String toString() {
        return "KafkaProperties{" +
                "servers='" + servers + '\'' +
                ", topicname='" + topicname + '\'' +
                ", doccount=" + doccount +
                ", consumergroup='" + consumergroup + '\'' +
                '}';
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
