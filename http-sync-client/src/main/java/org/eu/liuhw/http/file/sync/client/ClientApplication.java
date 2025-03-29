package org.eu.liuhw.http.file.sync.client;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.extra.spring.EnableSpringUtil;
import cn.hutool.log.StaticLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * @author javierHouse
 */
@EnableSpringUtil
@EnableScheduling
@SpringBootApplication(scanBasePackages = {"org.eu.liuhw"})
public class ClientApplication {

    public static void main(String[] args) {
        StaticLog.info(RuntimeUtil.execForStr("java -version"));
        SpringApplication.run(ClientApplication.class, args);
    }


    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat(DatePattern.NORM_DATETIME_PATTERN));
        mapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return mapper;
    }

}
