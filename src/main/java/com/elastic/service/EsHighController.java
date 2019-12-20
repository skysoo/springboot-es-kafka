package com.elastic.service;

import com.elastic.config.EsHighConfiguration;
import com.elastic.config.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2019-12-20 오후 2:03
 **/
@Slf4j
@RestController
@RequestMapping("/es_high")
public class EsHighController {
    private final Properties properties;
    private final EsHighConfiguration esHighConfiguration;

    public EsHighController(Properties properties, EsHighConfiguration esHighConfiguration) {
        this.properties = properties;
        this.esHighConfiguration = esHighConfiguration;
    }

    @GetMapping("/get_connection_check")
    public void esConnectionCheck(){
        esHighConfiguration.connectionCheck();
    }
}
