package org.eu.liuhw.http.file.sync.client.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.log.StaticLog;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.eu.liuhw.http.file.sync.base.entity.FileInfoVo;
import org.eu.liuhw.http.file.sync.base.entity.FilePathDto;
import org.eu.liuhw.http.file.sync.base.modle.RArray;
import org.eu.liuhw.http.file.sync.client.properties.SyncClientProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

    @Scheduled(initialDelay = 10, fixedDelayString = "#{T(java.lang.Long).parseLong(@environment.getProperty('sync.time')) * 1000}")
    public void sync() {
        init();
        final String target = syncClientProperties.getTarget();
        String root = "/";
        StaticLog.info("同步开始");
        final List<String> removes = new CopyOnWriteArrayList<>();
        syncPath(root, removes);
        for (String remove : removes) {
            final String path = target + remove;
            StaticLog.warn("删除 {}", path);
            FileUtil.del(path);
        }
        StaticLog.info("同步结束");
    }

    private void syncPath(String path, final List<String> removes) {
        final String target = syncClientProperties.getTarget();
        final String service = syncClientProperties.getService();

        try {

            final FilePathDto dto = new FilePathDto();
            dto.setP(path);
            final HttpRequest request = HttpRequest.post(syncClientProperties.getService() + "/.info/ls")
                    .timeout(syncClientProperties.getTimeout() * 1000)
                    .body(objectMapper.writeValueAsString(dto));

            auth(request);

            final HttpResponse response = request.execute();
            if (response.isOk()) {
                final RArray<FileInfoVo> r = objectMapper.readValue(response.body(), new TypeReference<RArray<FileInfoVo>>() {
                });
                //状态码200开始处理
                if (StrUtil.equals(r.getStatus(), "200")) {
                    final List<FileInfoVo> list = r.getData();
                    //本地目录
                    final String localPath = target + path;
                    //根据文件相对路径进行分组
                    final Map<String, List<FileInfoVo>> remoteMap = list.stream()
                            .collect(Collectors.groupingBy(FileInfoVo::getP));
                    //遍历本地文件
                    for (File file : FileUtil.ls(localPath)) {
                        //获取本地同名的目录和文件
                        final String local = StrUtil.replaceFirst(FileUtil.getAbsolutePath(file), target, "");
                        final List<FileInfoVo> infoVos = remoteMap.get(local);
                        if (CollUtil.isEmpty(infoVos)) {
                            //远端不存在该文件删除
                            removes.add(local);
                        }
                        //不是同时存在目录和文件
                        else if (infoVos.size() < 2) {
                            //比较远端文件
                            final FileInfoVo infoVo = infoVos.get(0);
                            if (FileUtil.isDirectory(file) && StrUtil.equals(infoVo.getD(), "1")) {
                                //目录。。不处理
                            }
                            else if (!FileUtil.isDirectory(file) && !StrUtil.equals(infoVo.getD(), "1")) {
                                //文件。。不处理
                            }
                            else {
                                removes.add(local);
                            }
                        }

                    }

                    for (FileInfoVo vo : list) {
                        if (StrUtil.equals(vo.getD(), "1")) {

                            final String newPath = target + vo.getP();
                            if (!FileUtil.exist(newPath)) {
                                StaticLog.warn("{} 地址为空，开始创建", newPath);
                                FileUtil.mkdir(newPath);
                            }


                            syncPath(vo.getP(), removes);

                        }
                        else {
                            final String newFile = target + vo.getP();

                            if (StrUtil.isNotBlank(vo.getS()) && FileUtil.exist(newFile) && StrUtil.equals(vo.getS(), DigestUtil.sha256Hex(FileUtil.getInputStream(newFile)))) {
                                StaticLog.info("文件已经存在 {}", newFile);
                            }
                            else {
                                requestDownload(service, vo, newFile);
                            }

                            ThreadUtil.sleep(10);
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
            StaticLog.error("同步失败 {}", target + path);
        }
    }

    /**
     * 鉴权
     *
     * @param request
     */
    private void auth(HttpRequest request) {
        if (StrUtil.isNotBlank(syncClientProperties.getKey())) {
            request.header("Authorization", syncClientProperties.getKey());
        }
    }

    /**
     * 发起文件下载请求
     *
     * @return
     */
    @SneakyThrows
    private void requestDownload(String service, FileInfoVo vo, String newFile) {
        final int timeout = syncClientProperties.getTimeout() * 1000;
        final String p = vo.getP();
        final String url = service + p;
        Assert.notBlank(url, "[url] is blank !");


        final StreamProgress progress = new StreamProgress() {
            @Override
            public void start() {
                StaticLog.info("下载文件开始 {}", p);
            }

            @Override
            public void progress(long total, long progressSize) {
                if (total == -1) {
                    StaticLog.info("{} 已经下载 {}", p, progressSize);
                }
                else {
                    StaticLog.info("{} {} 已经下载 {}， 总大小 {}", NumberUtil.formatPercent(NumberUtil.div(progressSize, total), 2), p, progressSize, total);
                }

            }

            @Override
            public void finish() {
                StaticLog.info("下载文件结束 {}", p);
            }
        };

        final Long rangeSize = syncClientProperties.getRangeSize();
        final Long l = vo.getL();
        if (syncClientProperties.getRange() && l > rangeSize) {
            //分区下载
            final int i = NumberUtil.div(new BigDecimal(l), rangeSize, 0, RoundingMode.UP).intValue();
            if (i > 9999) {
                StaticLog.error("range size {} exceeds the 9999 limit", i);
            }
            else {
                StaticLog.info("下载文件开始 {}", newFile);
                List<String> files = CollUtil.newArrayList();
                for (int j = 0; j < i; j++) {
                    final String range = StrFormatter.format("bytes={}-{}", j * rangeSize, (j + 1) * rangeSize -1 >= l ? "" : (j + 1) * rangeSize -1);
                    final HttpResponse response = getRequest(timeout, url).header("Range", range)
                            .executeAsync();
                    if (response.isOk()) {
                        final String pathname = newFile + StrFormatter.format(".{}.part", NumberUtil.decimalFormat("0000", j));
                        files.add(pathname);
                        response.writeBody(new File(pathname));
                    }
                    else {
                        throw new HttpException("Server response error with status code: [{}-{}] {}", response.getStatus(), range, response.body());
                    }
                }
                Files.deleteIfExists(Paths.get(newFile));
                try (FileChannel outChannel = FileChannel.open(Paths.get(newFile),
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                    for (String f : files) {
                        final Path path = Paths.get(f);
                        try (FileChannel inChannel = FileChannel.open(path)) {
                            inChannel.transferTo(0, inChannel.size(), outChannel);
                        }
                        Files.deleteIfExists(path);
                    }
                }
                StaticLog.info("下载文件结束 {}", newFile);
            }

        }
        else {
            //不分区下载
            final HttpResponse response = getRequest(timeout, url).executeAsync();
            if (response.isOk()) {

                response.writeBody(new File(newFile), ".part", progress);

                return;
            }

            throw new HttpException("Server response error with status code: [{}]", response.getStatus());
        }


    }


    private HttpRequest getRequest(int timeout, String url) {
        final HttpRequest request = HttpUtil.createGet(url, true);
        if (timeout > 0) {
            // 只有用户自定义了超时时长才有效，否则使用全局默认的超时时长。
            request.timeout(timeout);
        }
        auth(request);
        return request;
    }


}
