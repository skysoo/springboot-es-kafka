package com.elastic.config;

import org.apache.http.HttpHost;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2019-12-18 오전 10:29
 **/
@Configuration
public class EsHighConfiguration {
    private final Properties properties;
    private final InitHttpsIgnore initHttpsIgnore;

    public EsHighConfiguration(Properties properties, InitHttpsIgnore initHttpsIgnore) {
        this.properties = properties;
        this.initHttpsIgnore = initHttpsIgnore;
    }

    @Bean
    protected RestHighLevelClient restHighLevelClient() throws KeyManagementException, NoSuchAlgorithmException {
        initHttpsIgnore.initializeHttpConnection();
        System.out.println(properties.toString());
        return new RestHighLevelClient(RestClient.builder(
                                        new HttpHost(properties.getHost(),properties.getPort(),"https")).setHttpClientConfigCallback(
                new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                        return httpAsyncClientBuilder.setSSLHostnameVerifier(initHttpsIgnore.allHostsValid);
                    }
                }
        ));
    }

}
