package org.eu.liuhw.http.file.sync.client.properties;

import lombok.Getter;
import lombok.Setter;
import org.eu.liuhw.http.file.sync.base.util.IPathUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
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

    /**
     * 同步间隔时间
     */
    @NotNull(message = "配置sync.time不能为空")
    @Min(value = 10, message = "同步间隔时间最少10秒")
    @Max(value = Integer.MAX_VALUE, message = "同步间隔时间超出最大值")
    private Long time;

    @NotNull(message = "配置sync.time不能为空")
    @Min(value = 10, message = "同步间隔时间最少10秒")
    @Max(value = Integer.MAX_VALUE, message = "同步间隔时间超出最大值")
    private Integer timeout;

    private String key;


    public String getTarget() {
        return IPathUtil.rootPath(target);
    }
}
