package com.elastic.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2019-12-23 오전 10:28
 **/
@Slf4j
@Configuration
public class EsLowConfiguration {

    private final EsProperties esProperties;

    public EsLowConfiguration(EsProperties esProperties) {
        this.esProperties = esProperties;
    }

    public RestClient restLowLevelClient() {
        RestClient restClient = null;
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            InitHttpsIgnore.TrustManager(sc);

        restClient = RestClient.builder(new HttpHost(esProperties.getHost(), esProperties.getPort(), esProperties.getProtocol()))
                                                    .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                                                        @Override
                                                        public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                                                            return httpAsyncClientBuilder.setSSLContext(sc)
                                                                    .setSSLHostnameVerifier((hostname,session)->true);
                                                        }
                                                    }).build();

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

       return restClient;
    }

    public void getClusterStat(){
        Request request = new Request("GET","/_cluster/stats");
        try {
            Response response = restLowLevelClient().performRequest(request);
            log.info(response.toString());
        } catch (IOException e) {
            log.error("",e);
        }
    }
}
