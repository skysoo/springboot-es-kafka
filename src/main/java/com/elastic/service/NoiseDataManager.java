package com.elastic.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2019-12-19 오후 4:37
 **/
@Slf4j
@Component
public class NoiseDataManager {
    String getNoiseData(String fileName){
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
}
