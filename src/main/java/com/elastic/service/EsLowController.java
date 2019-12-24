package com.elastic.service;

import com.elastic.aop.LogExecutionTime;
import com.elastic.config.EsLowConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2019-12-23 오전 11:14
 **/
@Slf4j
@RestController
@RequestMapping("/es_low")
public class EsLowController {
    private final EsLowConfiguration esLowConfiguration;

    public EsLowController(EsLowConfiguration esLowConfiguration) {
        this.esLowConfiguration = esLowConfiguration;
    }

    @LogExecutionTime
    @GetMapping("/get_cluster_stat")
    public void esClusterStat(){
        esLowConfiguration.getClusterStat();
    }
}
