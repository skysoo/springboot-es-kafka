package com.elastic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2019-12-18 오전 10:52
 **/
@Configuration
public class AppRunner implements ApplicationRunner {
    @Autowired
    private EsService esService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
//        Thread.sleep(5000);
//        esService.esDataGet();
        esService.esIndexCreate("test1",3,2);
//        esService.esIndexCreate1();
    }
}
