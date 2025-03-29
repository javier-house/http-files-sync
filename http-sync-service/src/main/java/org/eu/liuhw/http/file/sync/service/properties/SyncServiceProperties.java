package org.eu.liuhw.http.file.sync.service.properties;

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
public class SyncServiceProperties {

    @NotNull(message = "配置sync.source不能为空")
    private String source;


    private String key;

    public String getSource() {
        return IPathUtil.rootPath(source);
    }
}
