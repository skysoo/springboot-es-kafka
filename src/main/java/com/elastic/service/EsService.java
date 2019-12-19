package com.elastic.service;

import com.elastic.config.EsLowConfiguration;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2019-12-18 오전 11:38
 **/
@Service
public class EsService {
    private final RestHighLevelClient restHighLevelClient;
    private final EsLowConfiguration esLowConfiguration;

    public EsService(RestHighLevelClient restHighLevelClient, EsLowConfiguration esLowConfiguration) {
        this.restHighLevelClient = restHighLevelClient;
        this.esLowConfiguration = esLowConfiguration;
    }

    public void esDataGet(){
        try {
            esLowConfiguration.get("https://192.168.10.7:31920/_cluster/stats");

        } catch (IOException | KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    void esIndexCreate(String indexName,int shardNum,int replicaNum){
        esLowConfiguration.put("https://192.168.10.7:31920/"+indexName+"?pretty","{\n" +
                "    \"settings\" : {\n" +
                "        \"index\" : {\n" +
                "            \"number_of_shards\" : "+shardNum+", \n" +
                "            \"number_of_replicas\" : "+replicaNum+" \n" +
                "        }\n" +
                "    }\n" +
                "}");
    }

    void esIndexCreate1(){
        esLowConfiguration.put("https://192.168.10.7:31920/twitter?pretty","{\n" +
                "    \"settings\" : {\n" +
                "        \"index\" : {\n" +
                "            \"number_of_shards\" : 3, \n" +
                "            \"number_of_replicas\" : 2 \n" +
                "        }\n" +
                "    }\n" +
                "}");
    }
}
