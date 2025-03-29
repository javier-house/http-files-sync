package org.eu.liuhw.http.file.sync.service;

import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.extra.spring.EnableSpringUtil;
import cn.hutool.log.StaticLog;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author javierHouse
 */
@EnableSpringUtil
@EnableScheduling
@SpringBootApplication(scanBasePackages = {"org.eu.liuhw"})
public class ServiceApplication {
    public static void main(String[] args) {
        StaticLog.info(RuntimeUtil.execForStr("java -version"));
        SpringApplication.run(ServiceApplication.class, args);
    }
}
