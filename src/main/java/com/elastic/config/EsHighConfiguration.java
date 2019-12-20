package com.elastic.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2019-12-18 오전 10:29
 **/
@Slf4j
@Configuration
public class EsHighConfiguration {
    private final Properties properties;
    private final InitHttpsIgnore initHttpsIgnore;

    public EsHighConfiguration(Properties properties, InitHttpsIgnore initHttpsIgnore) {
        this.properties = properties;
        this.initHttpsIgnore = initHttpsIgnore;
    }

    @Bean
    protected RestHighLevelClient restHighLevelClient() {
        log.info(properties.toString());
        return new RestHighLevelClient(RestClient.builder(
                                        new HttpHost(properties.getHost(),properties.getPort(),"https"))
                                        .setHttpClientConfigCallback(httpAsyncClientBuilder ->
                                                httpAsyncClientBuilder.setSSLHostnameVerifier((hostname,session)->true)
                                        ));
    }


    public void connectionCheck(){
        try {
            boolean esPingResult = restHighLevelClient().ping(RequestOptions.DEFAULT);
            if (esPingResult) log.info("##### Es Server Connection is Normal. ");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
