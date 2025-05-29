package org.eu.liuhw.http.file.sync.service.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.log.StaticLog;
import org.eu.liuhw.http.file.sync.base.entity.FileInfoVo;
import org.eu.liuhw.http.file.sync.base.entity.FilePathDto;
import org.eu.liuhw.http.file.sync.base.modle.R;
import org.eu.liuhw.http.file.sync.base.modle.RArray;
import org.eu.liuhw.http.file.sync.service.properties.SyncServiceProperties;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author JavierHouse
 */
@RestController
@RequestMapping(".info")
public class FileController {

    //改为properties方式获取
    /*@Value("${sync.source}")
    private String source;*/

    @Resource
    private SyncServiceProperties syncServiceProperties;

    @PostConstruct
    public void init() {
        final String source = syncServiceProperties.getSource();
        if (!FileUtil.exist(source)) {
            StaticLog.warn("{} 地址为空，开始创建", source);
            FileUtil.mkdir(source);
        }
    }

    @PostMapping("ls")
    public RArray<FileInfoVo> ls(@RequestBody FilePathDto dto) {
        final String source = syncServiceProperties.getSource();
        final List<File> ls = Stream.of(FileUtil.ls(source + Optional.ofNullable(dto.getP()).orElse(StrUtil.EMPTY)))
                .sorted(Comparator.comparing(f -> {
                    File file = (File) f;
                    try {
                        if (!FileUtil.exist(file)) {
                            return 0L;
                        }
                        return file.lastModified();
                    } catch (Exception e) {
                        StaticLog.error(e);
                        return 0L;
                    }
                }).reversed())
                .collect(Collectors.toList());

        final List<FileInfoVo> result = new ArrayList<>();
        for (File file : ls) {
            final String name = FileUtil.getName(file);
            final FileInfoVo vo = new FileInfoVo();
            final boolean directory = FileUtil.isDirectory(file);
            if (directory) {
                //不同步隐藏目录
                if (StrUtil.startWith(name, ".")) {
                    continue;
                }
                vo.setD("1");
            }


            vo.setP(StrUtil.replaceFirst(FileUtil.getAbsolutePath(file), source, ""));

            final long length = file.length();

            vo.setL(length);

            if (!directory) {
                //不是目录计算校验值
                vo.setS(DigestUtil.sha256Hex(FileUtil.getInputStream(file)));
            }

            result.add(vo);
        }
        return R.ok(result);
    }

}
