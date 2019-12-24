package com.elastic.config;

import com.elastic.service.NoiseDataManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2019-12-18 오전 10:29
 **/
@Slf4j
@Configuration
public class EsHighConfiguration {
    private final Properties properties;
    private final ObjectMapper objectMapper;
    private final NoiseDataManager noiseDataManager;

    public EsHighConfiguration(Properties properties, NoiseDataManager noiseDataManager, ObjectMapper objectMapper) {
        this.properties = properties;
        this.noiseDataManager = noiseDataManager;
        this.objectMapper = objectMapper;
    }

    @Bean
    protected RestHighLevelClient restHighLevelClient() {
        RestHighLevelClient restHighLevelClient = null;

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            InitHttpsIgnore.TrustManager(sc);

            restHighLevelClient = new RestHighLevelClient(RestClient.builder(
                    new HttpHost(properties.getHost(), properties.getPort(), properties.getProtocol()))
                    .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                        @Override
                        public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                            return httpAsyncClientBuilder.setSSLContext(sc)
                                    .setSSLHostnameVerifier((hostname, session) -> true);
                        }
                    })
            );

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("",e);
        }

        log.info(properties.toString());

        return restHighLevelClient;
    }

    public void connectionCheck() {
        boolean esPingResult = false;
        try {
            esPingResult = restHighLevelClient().ping(RequestOptions.DEFAULT);
            if (esPingResult) log.info("##### Es Server Connection is Normal. ");
        } catch (IOException e) {
            log.error("",e);
        }
    }

    public void searchByIndexName(String indexName){
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;

        try {
            searchResponse = restHighLevelClient().search(searchRequest,RequestOptions.DEFAULT);
            if(searchResponse!=null)
                log.info(searchResponse.toString());
        } catch (IOException e) {
            log.error("",e);
        }
    }

    public void createIndex(String indexName,int shardNum,int replicaNum){
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
        createIndexRequest.settings(Settings.builder()
                .put("index.number_of_shards", shardNum)
                .put("index.number_of_replicas", replicaNum));
//                .put("index.codec","best_compression")
//                .put("index.provided_name",indexName));

        ActionListener<CreateIndexResponse> listener = new ActionListener<CreateIndexResponse>() {
            @Override
            public void onResponse(CreateIndexResponse createIndexResponse) {
                boolean acknowledged = createIndexResponse.isAcknowledged();
                if (acknowledged) log.info("##### Create Index {}", indexName);
            }

            @Override
            public void onFailure(Exception e) {
                log.error("{}",indexName,e);
            }
        };
        restHighLevelClient().indices().createAsync(createIndexRequest,RequestOptions.DEFAULT,listener);
    }

    public void deleteIndex(String indexName){
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
        deleteIndexRequest.timeout(TimeValue.timeValueMinutes(1));

        ActionListener<AcknowledgedResponse> listener = new ActionListener<AcknowledgedResponse>() {
            @Override
            public void onResponse(AcknowledgedResponse deletedIndexResponse) {
                boolean acknowledged = deletedIndexResponse.isAcknowledged();
                if (acknowledged) log.info("##### Delete index {}",indexName);
            }

            @Override
            public void onFailure(Exception e) {
                log.error("{}",indexName,e);
            }
        };
        restHighLevelClient().indices().deleteAsync(deleteIndexRequest,RequestOptions.DEFAULT,listener);
    }

    public void bulkProcessorByIndexName(String indexName){
        BulkRequest bulkRequest = new BulkRequest();
//        bulkRequest.add(getData("test_data_small_1.txt"));

        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            /**
             * This method is called before each execution of a BulkRequest
             **/
            @Override
            public void beforeBulk(long executionId, BulkRequest bulkRequest) {
                int numberOfActions = bulkRequest.numberOfActions();
                log.debug("Executing bulk [{}] with {} requests",
                        executionId, numberOfActions);
            }
            /**
             * This method is called after each execution of a BulkRequest
             **/
            @Override
            public void afterBulk(long executionId, BulkRequest bulkRequest, BulkResponse bulkResponse) {
                if (bulkResponse.hasFailures()) {
                    log.warn("Bulk [{}] executed with failures", executionId);
                } else {
                    log.debug("Bulk [{}] completed in {} milliseconds",
                            executionId, bulkResponse.getTook().getMillis());
                }
            }
            /**
             * This method is called when a BulkRequest failed
             **/
            @Override
            public void afterBulk(long executionId, BulkRequest bulkRequest, Throwable failure) {
                log.error("Failed to execute bulk", failure);
            }
        };

        BulkProcessor.Builder bulkProcessorBuilder = BulkProcessor.builder(
                (request,bulkListener) ->
                        restHighLevelClient().bulkAsync(bulkRequest,RequestOptions.DEFAULT,bulkListener),
                listener)
                .setBulkActions(5000)
                .setBulkSize(new ByteSizeValue(30, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueSeconds(5))
                .setConcurrentRequests(1)
                .setBackoffPolicy(
                        BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100),3));

        BulkProcessor bulkProcessor = bulkProcessorBuilder.build();

//        bulkProcessor.add(getData("test_data_small_1.txt"));

    }

    public void bulkByIndex(String fileName){
        BulkRequest request = new BulkRequest();
        LinkedHashMap<String,String> mapData = new LinkedHashMap<String,String>();

        List<LinkedHashMap<String,String>> indexRequestList = new ArrayList<>();

        for (int i=0;i<10;i++){
            mapData.put(String.valueOf(getData(fileName).hashCode()),getData(fileName));
        }
        indexRequestList.add(mapData);

        Stream<IndexRequest> indexRequestStream =indexRequestList.stream().map(stream -> new IndexRequest()
                                                                        .index("webtest")
                                                                        .source(stream));

        IndexRequest[] indexRequests = indexRequestStream.toArray(IndexRequest[]::new);
        log.info(String.valueOf(indexRequests.length));

        request.add(indexRequests);

        ActionListener<BulkResponse> listener = new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse bulkItemResponses) {
                log.info("##### Success Bulk Data {}",fileName);
            }

            @Override
            public void onFailure(Exception e) {
                log.error("",e);
            }
        };
//        restHighLevelClient().bulkAsync(request,RequestOptions.DEFAULT,listener);
        try {
            restHighLevelClient().bulk(request,RequestOptions.DEFAULT);
            log.info("##### Success Bulk Data {}",fileName);
        } catch (IOException e) {
            log.error("",e);
        }
    }

    public String getData(String fileName){
        return noiseDataManager.getNoiseData(fileName);
    }

    public void getRandomData(String fileName){
        noiseDataManager.getRandomNoiseData(fileName);
    }

    public void getRandomRealData(String fileName) throws IOException {
        noiseDataManager.getRandomRealData(fileName);
    }
}
