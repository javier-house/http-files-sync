package org.eu.liuhw.http.file.sync.base.entity;

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
     * 是否是文件夹
     */
    private String d;

}
