package org.eu.liuhw.http.file.sync.service.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.Setter;
import org.eu.liuhw.http.file.sync.service.properties.SyncServiceProperties;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthFilter extends OncePerRequestFilter {

    @Setter
    private SyncServiceProperties syncServiceProperties;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        // 包装原始请求
        final ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);

        // 跳过登录、公开接口等路径
        if (CollUtil.newArrayList(SpringUtil.getActiveProfiles()).contains("dev")) {
            if (isPublicEndpoint(wrappedRequest)) {
                chain.doFilter(wrappedRequest, response);
                return;
            }
        }


        if (StrUtil.isBlank(syncServiceProperties.getKey())) {
            chain.doFilter(wrappedRequest, response);
            return;
        }
        if (StrUtil.equals(syncServiceProperties.getKey(), ServletUtil.getHeaderIgnoreCase(wrappedRequest, "Authorization"))) {
            chain.doFilter(wrappedRequest, response);
            return;
        }

        sendError(response, 403, "Insufficient permissions");
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/doc.html")
                || path.startsWith("/webjars")
                || path.startsWith("/v3/api-docs/");
    }


    private void sendError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.getWriter().write(message);
        response.getWriter().flush();
    }


}
