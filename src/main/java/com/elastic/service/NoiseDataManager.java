package com.elastic.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2019-12-19 오후 4:37
 **/
@Slf4j
@Component
public class NoiseDataManager {
    public String getNoiseData(String fileName){
        Path path = Paths.get("D:\\99.TEMP\\"+fileName);
        Charset cs = StandardCharsets.UTF_8;
        List<String> list = new ArrayList<String>();
        try {
            list = Files.readAllLines(path,cs);
        } catch (IOException e) {
            log.error("",e);
        }
        return list.stream().map(String::valueOf)
                        .collect(Collectors.joining());
    }

    public void getRandomNoiseData(String fileName){
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile("D:\\99.TEMP\\"+fileName,"r");
            long random = new Random().nextInt((int) randomAccessFile.length());
            log.debug("##### Random file length is {}",randomAccessFile.length());

            // random 위치에서부터 지정된 크기 만큼 파일 읽기
            randomAccessFile.seek(random);
            log.info("##### Random int is {}",random);
            byte[] dataBytes = new byte[100];
            randomAccessFile.readFully(dataBytes);

            // 크기로 자르기 때문에 첫 개행문자와 마지막 개행문자 사이의 값이 의미있는 값
            String byteStr = new String(dataBytes);
            int start = byteStr.indexOf("\n");
            int end = byteStr.lastIndexOf("\n");

            String data = (String) byteStr.subSequence(start,end);
            log.info(String.valueOf(data.length()));
            log.info("개행 문자 유 : {}",data);

            // 문자열 중 개행문자 치환
            data = data.replaceAll("\\n"," ");
            log.info("개행 문자 무 : {}",data);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getRandomRealData(String fileName) throws IOException {
        File file = new File("D:\\99.TEMP\\"+fileName);
        RandomAccessFile randomAccessFile = new RandomAccessFile(file,"r");
        long random = new Random().nextInt((int) (randomAccessFile.length()-11400000));
        log.info("##### File is {}","D:\\99.TEMP\\"+fileName);

        // random 위치에서부터 지정된 크기 만큼 파일 읽기
        randomAccessFile.seek(random);

        String line="";
        StringBuilder sb = new StringBuilder();
        int count=0;
        List<String> noiseList = new ArrayList<>(60);

        while ((line=randomAccessFile.readLine()) != null){
            if(count!=0){
                String[] temp = line.split(",");
                sb.append(temp[1]).append(" ");
            }
            count++;

            if (count%10000==0){
                noiseList.add(String.valueOf(sb));
                sb = new StringBuilder();
            }
            if (count==30000) break;
        }
        randomAccessFile.close();
        log.info("count : {}",count);
        log.info("noise List length : {}",noiseList.size());
    }
}
