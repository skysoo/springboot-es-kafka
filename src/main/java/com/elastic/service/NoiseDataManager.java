package com.elastic.service;

import com.google.gson.internal.LinkedTreeMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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
        Path path = Paths.get(fileName);
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

    /**
     * fileName : Bulk Data File Path
     * lineNum : number of line
     * bundleNum : number of bundle
     **/
    public LinkedTreeMap<String, String> getRandomAccessData(String fileName, int lineNum, int bundleNum) throws IOException {
        File file = new File(fileName);
        RandomAccessFile randomAccessFile = new RandomAccessFile(file,"r");
        long random = new Random().nextInt((int) (randomAccessFile.length()-11400000));
//        long random = new Random().nextInt((int) (randomAccessFile.length()));
        log.info("##### File is {}",fileName);

        // random 위치에서부터 지정된 크기 만큼 파일 읽기
        randomAccessFile.seek(random);

        String line="";
        StringBuilder sb = new StringBuilder();
        int count=0;
        LinkedTreeMap<String, String> noiseMap = new LinkedTreeMap<>();


        while ((line=randomAccessFile.readLine()) != null){
            if(count!=0){
                String[] temp = line.split(",");
                sb.append(temp[1]).append(" ");
            }
            count++;

            if (count%lineNum==0){
                log.info("##### current count : {}",count);
            }
            if (count==bundleNum){
                noiseMap.put(String.valueOf(sb.toString().hashCode()),sb.toString());
                break;
            }
        }
        randomAccessFile.close();
        log.info("noise count : {}",count);
        log.info("noise Map length : {}",noiseMap.size());

        return noiseMap;
    }

    public List<String> getFileList(String path){
        File fileDir = new File(path);
        File[] files = fileDir.listFiles();

        assert files != null;
        List<String> fileList = new ArrayList<String>();

        for (File file : files){
            if (file.isFile())
                fileList.add(String.valueOf(file.getAbsoluteFile()));
        }
        return fileList;
    }
}
