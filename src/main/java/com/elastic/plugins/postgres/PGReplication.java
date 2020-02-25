//package com.elastic.plugins.postgres;
//
//import lombok.extern.slf4j.Slf4j;
//import org.postgresql.PGConnection;
//import org.postgresql.PGProperty;
//import org.postgresql.replication.PGReplicationStream;
//
//import java.nio.ByteBuffer;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.util.Properties;
//import java.util.concurrent.TimeUnit;
//
///**
// * @author skysoo
// * @version 1.0.0
// * @since 2020-02-10 오후 4:36
// **/
//@Slf4j
//public class PGReplication {
//    public static void main(String[] args) throws SQLException, InterruptedException {
//        String url = "jdbc:postgresql://192.168.10.7:5432/postgres";
//
//        Properties props = new Properties();
//        PGProperty.USER.set(props, "postgres");
//        PGProperty.PASSWORD.set(props, "postgres");
//        PGProperty.ASSUME_MIN_SERVER_VERSION.set(props, "11.5");
//        PGProperty.REPLICATION.set(props, "database");
//        PGProperty.PREFER_QUERY_MODE.set(props, "simple");
//
//        System.out.println("DEBUG100");
//        Connection con = DriverManager.getConnection(url, props);
//        PGConnection replConnection = con.unwrap(PGConnection.class);
//        System.out.println("DEBUG200");
//
////        replConnection.getReplicationAPI()
////                .createReplicationSlot()
////                .logical()
////                .withSlotName("repl_slot_02")
////                .withOutputPlugin("test_decoding")
////                .make();
//
//        PGReplicationStream stream =
//                replConnection.getReplicationAPI()
//                        .replicationStream()
//                        .logical()
//                        .withSlotName("repl_slot_02")
//                        .withSlotOption("include-xids", false)
//                        .withSlotOption("skip-empty-xacts", true)
//                        .withStatusInterval(20, TimeUnit.SECONDS)
//                        .start();
//
//        while (true) {
//            //non blocking receive message
//            ByteBuffer msg = stream.readPending();
//
//            if (msg == null) {
//                TimeUnit.MILLISECONDS.sleep(10L);
//                continue;
//            }
//
//            int offset = msg.arrayOffset();
//            byte[] source = msg.array();
//            int length = source.length - offset;
////            System.out.println(new String(source, offset, length));
//            log.info(new String(source,offset,length));
//
//            //feedback
//            stream.setAppliedLSN(stream.getLastReceiveLSN());
//            stream.setFlushedLSN(stream.getLastReceiveLSN());
//        }
//    }
//
//}
