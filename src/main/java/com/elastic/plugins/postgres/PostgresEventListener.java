package com.elastic.plugins.postgres;

import com.elastic.configuration.CommonProperties;
import com.elastic.configuration.EsProperties;
import com.elastic.configuration.KafkaProperties;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;
import org.postgresql.PGProperty;
import org.postgresql.replication.PGReplicationStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2020-02-11 오후 4:44
 **/
@Slf4j
@Component
public class PostgresEventListener implements ApplicationRunner {
    @Autowired
    private CommonProperties commonProperties;
    @Autowired
    private EsProperties esProperties;
    @Autowired
    private KafkaProperties kafkaProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        init();
        String url = "jdbc:postgresql://192.168.10.7:5432/postgres";

        Properties props = new Properties();
        PGProperty.USER.set(props, "postgres");
        PGProperty.PASSWORD.set(props, "postgres");
        PGProperty.ASSUME_MIN_SERVER_VERSION.set(props, "11.5");
        PGProperty.REPLICATION.set(props, "database");
        PGProperty.PREFER_QUERY_MODE.set(props, "simple");

        System.out.println("DEBUG100");
        try {
            Connection con = DriverManager.getConnection(url, props);
            PGConnection replConnection = con.unwrap(PGConnection.class);
            System.out.println("DEBUG200");

//        replConnection.getReplicationAPI()
//                .createReplicationSlot()
//                .logical()
//                .withSlotName("repl_slot_02")
//                .withOutputPlugin("test_decoding")
//                .make();

            PGReplicationStream stream = replConnection.getReplicationAPI()
                    .replicationStream()
                    .logical()
                    .withSlotName("repl_slot_02")
                    .withSlotOption("include-xids", false)
                    .withSlotOption("skip-empty-xacts", true)
                    .withStatusInterval(20, TimeUnit.SECONDS)
                    .start();

            while (true) {
                //non blocking receive message
                ByteBuffer msg = stream.readPending();

                if (msg == null) {
                    TimeUnit.MILLISECONDS.sleep(10L);
                    continue;
                }

                int offset = msg.arrayOffset();
                byte[] source = msg.array();
                int length = source.length - offset;
//            System.out.println(new String(source, offset, length));
                log.info(new String(source, offset, length));

                //feedback
                stream.setAppliedLSN(stream.getLastReceiveLSN());
                stream.setFlushedLSN(stream.getLastReceiveLSN());
            }
        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    void init(){
        log.info("#####################################################");
        log.info("################ Properties Value ###################");
        log.info("producer thread count : {}",commonProperties.getProducerthreadcount());
        log.info("consumer thread count : {}",commonProperties.getConsumerthreadcount());
        log.info("es cluster : {}",esProperties.getCluster());
        log.info("es host : {}",esProperties.getHost());
        log.info("es port : {}",esProperties.getPort());
        log.info("es protocol : {}",esProperties.getProtocol());
        log.info("kafka server : {}",kafkaProperties.getServers());
        log.info("kafka topic : {}",kafkaProperties.getTopicname());
        log.info("kafka consumer group : {}",kafkaProperties.getConsumergroup());
        log.info("kafka doc count : {}",kafkaProperties.getDoccount());
        log.info("kafka giveup count : {}",kafkaProperties.getGiveup());
        log.info("kafka poll duration : {}",kafkaProperties.getPollduration());
        log.info("kafka poll interval : {}",kafkaProperties.getPollinterval());
        log.info("kafka poll records count : {}",kafkaProperties.getPollrecords());
        log.info("#####################################################");
    }
}
