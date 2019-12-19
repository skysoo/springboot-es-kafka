package com.elastic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@EnableConfigurationProperties
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MainEsDemo {
    private static MainEsDemo O = new MainEsDemo();

    public static void main(String[] args) {
        getInstance()._main(args);
    }

    static MainEsDemo getInstance(){
        return O;
    }

    void _main(String[] args) {
        SpringApplication.run(MainEsDemo.class, args);

    }
}
