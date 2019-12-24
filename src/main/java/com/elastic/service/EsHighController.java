package com.elastic.service;

import com.elastic.aop.LogExecutionTime;
import com.elastic.config.EsHighConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2019-12-20 오후 2:03
 **/
@Slf4j
@RestController
@RequestMapping("/es_high")
public class EsHighController {
    private final EsHighConfiguration esHighConfiguration;

    public EsHighController(EsHighConfiguration esHighConfiguration) {
        this.esHighConfiguration = esHighConfiguration;
    }

    @LogExecutionTime
    @GetMapping("/get_connection_check")
    public void esConnectionCheck(){
        esHighConfiguration.connectionCheck();
    }

    @LogExecutionTime
    @GetMapping("/get_search_all")
    public void esSearchAll(@RequestParam String indexName){
        esHighConfiguration.searchByIndexName(indexName);
    }

    @LogExecutionTime
    @PutMapping("/put_create_index")
    public void esCreateIndex(@RequestParam String indexName, @RequestParam int shardNum, @RequestParam int replicaNum){
        esHighConfiguration.createIndex(indexName,shardNum,replicaNum);
    }

    @LogExecutionTime
    @DeleteMapping("/delete_index")
    public void esDeleteIndex(@RequestParam String indexName){
        esHighConfiguration.deleteIndex(indexName);
    }

    @LogExecutionTime
    @PostMapping("/post_bulk_index")
    public void esBulkIndex(@RequestParam String fileName) throws IOException {
//        esHighConfiguration.bulkByIndex(fileName);
//        esHighConfiguration.getRandomData(fileName);
        esHighConfiguration.getRandomRealData(fileName);
    }

    @LogExecutionTime
    @PostMapping("/post_bulk_processor_index")
    public void esBulkProcessorIndex(@RequestParam String indexName){
        esHighConfiguration.bulkProcessorByIndexName(indexName);
    }

}
