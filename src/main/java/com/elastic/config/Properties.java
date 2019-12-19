package com.elastic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2019-12-18 오전 10:40
 **/
@Component
@ConfigurationProperties(prefix = "elasticsearch")
public class Properties {
    private String host;
    private int port;
    private String cluster;

    @Override
    public String toString() {
        return "Properties{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", cluster='" + cluster + '\'' +
                '}';
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }
}
