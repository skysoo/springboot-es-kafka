package com.elastic.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2019-12-18 오전 10:40
 **/
@Component
@ConfigurationProperties(prefix = "elasticsearch")
public class EsProperties {
    // 통신 프로토콜 , http, https
    private String protocol;
    // ES 서버 주소
    private String host;
    // ES 포트
    private int port;
    // ES cluster 이름
    private String cluster;
    // 데이터 파일 이름
    private String filepath;
    // ES 인덱스 이름
    private String indexname;


    public String getIndexname() {
        return indexname;
    }

    public void setIndexname(String indexname) {
        this.indexname = indexname;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
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

    @Override
    public String toString() {
        return "Properties{" +
                "protocol='" + protocol + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", cluster='" + cluster + '\'' +
                '}';
    }
}
