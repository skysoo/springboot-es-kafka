package com.elastic.plugins.postgres.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author skysoo
 * @version 1.0.0
 * @since 2020-03-18 오전 10:58
 **/
@Slf4j
@Getter
@Setter
@ToString
public class User {
    private Long pid;
    private String name;
    private String password;
}
