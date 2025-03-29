package org.eu.liuhw.http.file.sync.client.properties;

import lombok.Getter;
import lombok.Setter;
import org.eu.liuhw.http.file.sync.base.util.IPathUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * @author JavierHouse
 */
@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "sync")
public class SyncClientProperties {

    @NotNull(message = "配置sync.target不能为空")
    private String target;

    @NotNull(message = "配置sync.service不能为空")
    private String service;

    public String getTarget() {
        return IPathUtil.rootPath(target);
    }
}
