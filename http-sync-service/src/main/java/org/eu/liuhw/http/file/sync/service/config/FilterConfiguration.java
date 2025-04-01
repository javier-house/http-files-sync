package org.eu.liuhw.http.file.sync.service.config;

import org.eu.liuhw.http.file.sync.service.filter.AuthFilter;
import org.eu.liuhw.http.file.sync.service.properties.SyncServiceProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import javax.annotation.Resource;
import javax.servlet.DispatcherType;
import javax.servlet.annotation.MultipartConfig;

@Configuration
@MultipartConfig
public class FilterConfiguration {
    @Resource
    private SyncServiceProperties syncServiceProperties;

    @Bean
    FilterRegistrationBean<AuthFilter> tokenAuthFilter() {
        AuthFilter filter = new AuthFilter();
        filter.setSyncServiceProperties(syncServiceProperties);
        FilterRegistrationBean<AuthFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(filter);
        bean.addUrlPatterns("/*");
        bean.setName("authFilter");
        bean.setOrder(Ordered.LOWEST_PRECEDENCE);
        bean.setDispatcherTypes(DispatcherType.REQUEST);

        return bean;
    }


}
