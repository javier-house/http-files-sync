package org.eu.liuhw.http.file.sync.base.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

/**
 * @author JavierHouse
 */
@Getter
@Setter
public class FileInfoVo {

    /**
     * 文件相对路径
     */
    private String p;
    /**
     * SHA-256摘要值
     */
    private String s;

    /**
     * 是否是目录
     */
    private String d;

    /**
     * 文件大小
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long l;

}
