package org.eu.liuhw.http.file.sync.base.util;

import cn.hutool.core.util.StrUtil;

/**
 * @author JavierHouse
 */
public class IPathUtil {
    public static String rootPath(String path) {
        if (StrUtil.isBlank(path)) {
            return path;
        }
        path = path.replaceAll("\\\\", "/");
        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int i = path.length(); i > 0; i--) {
            final String sub = StrUtil.sub(path, i - 1, i);
            if (first) {
                if (StrUtil.equals("/", sub)) {
                    continue;
                }
                else {
                    first = false;
                }
            }
            sb.append(sub);
        }

        return sb.reverse().toString();
    }
}
