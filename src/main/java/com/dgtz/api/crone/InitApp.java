package com.dgtz.api.crone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * BroCast.
 * Copyright: Sardor Navruzov
 * 2013-2017.
 */

@SpringBootApplication
@EnableScheduling
public class InitApp {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(InitApp.class, args);
    }
}
