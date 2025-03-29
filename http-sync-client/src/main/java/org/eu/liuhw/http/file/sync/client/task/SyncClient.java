package org.eu.liuhw.http.file.sync.client.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.log.StaticLog;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eu.liuhw.http.file.sync.base.entity.FileInfoVo;
import org.eu.liuhw.http.file.sync.base.entity.FilePathDto;
import org.eu.liuhw.http.file.sync.base.modle.RArray;
import org.eu.liuhw.http.file.sync.client.properties.SyncClientProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author JavierHouse
 */
@Component
public class SyncClient {

    @Resource
    private SyncClientProperties syncClientProperties;

    @Resource
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        final String target = syncClientProperties.getTarget();
        if (!FileUtil.exist(target)) {
            StaticLog.warn("{} 地址为空，开始创建", target);
            FileUtil.mkdir(target);
        }
    }

    @Scheduled(initialDelay = 10, fixedDelay = 600000)
    public void sync() {
        final String target = syncClientProperties.getTarget();
        String root = "/";
        StaticLog.info("同步开始");
        final List<String> removes = new CopyOnWriteArrayList<>();
        syncPath(root, removes);
        for (String remove : removes) {
            final String path = syncClientProperties.getTarget() + remove;
            StaticLog.warn("删除 {}", path);
            FileUtil.del(path);
        }
        StaticLog.info("同步结束");
    }

    private void syncPath(String path, final List<String> removes) {
        try {


            final FilePathDto dto = new FilePathDto();
            dto.setP(path);
            final HttpResponse response = HttpRequest.post(syncClientProperties.getService() + "/.info/ls")
                    .timeout(60 * 1000)
                    .body(objectMapper.writeValueAsString(dto))
                    .execute();
            if (response.isOk()) {
                final RArray<FileInfoVo> r = objectMapper.readValue(response.body(), new TypeReference<RArray<FileInfoVo>>() {
                });
                if (StrUtil.equals(r.getStatus(), "200")) {
                    final List<FileInfoVo> list = r.getData();

                    final String localPath = syncClientProperties.getTarget() + path;

                    final Map<String, List<FileInfoVo>> remoteMap = list.stream()
                            .collect(Collectors.groupingBy(FileInfoVo::getP));

                    for (File file : FileUtil.ls(localPath)) {
                        final String local = StrUtil.replaceFirst(FileUtil.getAbsolutePath(file), syncClientProperties.getTarget(), "");
                        final List<FileInfoVo> infoVos = remoteMap.get(local);
                        if (CollUtil.isEmpty(infoVos)) {
                            removes.add(local);
                        }
                        else if (infoVos.size() < 2) {
                            final FileInfoVo infoVo = infoVos.get(0);
                            if (FileUtil.isDirectory(file) && StrUtil.equals(infoVo.getD(), "1")) {
                            }
                            else if (!FileUtil.isDirectory(file) && !StrUtil.equals(infoVo.getD(), "1")) {
                            }
                            else {
                                removes.add(local);
                            }
                        }

                    }

                    for (FileInfoVo vo : list) {
                        if (StrUtil.equals(vo.getD(), "1")) {

                            final String newPath = syncClientProperties.getTarget() + vo.getP();
                            if (!FileUtil.exist(newPath)) {
                                StaticLog.warn("{} 地址为空，开始创建", newPath);
                                FileUtil.mkdir(newPath);
                            }


                            syncPath(vo.getP(), removes);

                        }
                        else {
                            final String newFile = syncClientProperties.getTarget() + vo.getP();

                            if (StrUtil.isNotBlank(vo.getS()) && FileUtil.exist(newFile) && StrUtil.equals(vo.getS(), DigestUtil.sha256Hex(FileUtil.getInputStream(newFile)))) {
                                StaticLog.info("文件已经存在 {}", newFile);
                            }
                            else {

                                HttpUtil.download(syncClientProperties.getService() + vo.getP(), FileUtil.getOutputStream(newFile), Boolean.TRUE, new StreamProgress() {
                                    @Override
                                    public void start() {
                                        StaticLog.info("下载文件开始 {}", vo.getP());
                                    }

                                    @Override
                                    public void progress(long total, long progressSize) {
                                        if (total == -1) {
                                            StaticLog.info("{} 已经下载 {}", vo.getP(), progressSize);
                                        }
                                        else {
                                            StaticLog.info("{} {} 已经下载 {}， 总大小 {}", NumberUtil.formatPercent(NumberUtil.div(progressSize, total), 2), vo.getP(), progressSize, total);
                                        }

                                    }

                                    @Override
                                    public void finish() {
                                        StaticLog.info("下载文件结束 {}", vo.getP());
                                    }
                                });


                            }

                            ThreadUtil.sleep(200);
                        }


                    }

                }
                else {
                    StaticLog.error("同步失败 {},{}", r.getStatus(), r.getMessage());
                }

            }
            else {
                StaticLog.error("同步失败 {},{}", response.getStatus(), response.body());
            }
        } catch (Exception e) {
            StaticLog.error(e);
            StaticLog.error("同步失败 {}", syncClientProperties.getTarget() + path);
        }
    }

}
