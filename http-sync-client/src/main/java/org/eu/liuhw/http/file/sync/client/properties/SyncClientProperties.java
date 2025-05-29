package org.eu.liuhw.http.file.sync.client.properties;

import lombok.Getter;
import lombok.Setter;
import org.eu.liuhw.http.file.sync.base.util.IPathUtil;
import org.eu.liuhw.http.file.sync.base.validation.ConditionalNotNull;
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

    /**
     * 密钥
     */
    private String key;

    /**
     * 是否开启文件分区下载
     */
    @NotNull(message = "文件分区下载配置不能为空")
    private Boolean range = false;



    /**
     * 文件分片字节数
     */
    @ConditionalNotNull(message = "range为true，rangeSize不能为空",field = "range", expectedValue = "true")
    @Min(value = 1024, message = "分片大小不能低于1024字节")
    @Max(value = Integer.MAX_VALUE, message = "同步间隔时间超出最大值")
    private Long rangeSize;



    public String getTarget() {
        return IPathUtil.rootPath(target);
    }
}
