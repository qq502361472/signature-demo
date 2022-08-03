package com.hjrpc.signaturedemo.filter;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.hjrpc.signaturedemo.inputstream.BodyReaderRequestWrapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Slf4j
@Order(1)
public class RequestCachingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response
            , FilterChain filterChain) {
        try {
            filterChain.doFilter(new BodyReaderRequestWrapper(request), response);
        } catch (IOException | ServletException e) {
            log.error("RequestCachingFilter异常：", e);
            printRequest(request);
        }
    }

    private void printRequest(HttpServletRequest request) {
        String body = StrUtil.EMPTY;
        try {
            if (request instanceof BodyReaderRequestWrapper) {
                body = IoUtil.readUtf8(request.getInputStream());
            }
        } catch (IOException e) {
            log.error("printRequest 获取body异常...", e);
        }

        JSONObject requestJ = new JSONObject();
        JSONObject headers = new JSONObject();
        Collections.list(request.getHeaderNames()).forEach(name -> headers.set(name, request.getHeader(name)));
        requestJ.set("headers", headers);
        requestJ.set("parameters", request.getParameterMap());
        requestJ.set("body", body);
        requestJ.set("remote-user", request.getRemoteUser());
        requestJ.set("remote-addr", request.getRemoteAddr());
        requestJ.set("remote-host", request.getRemoteHost());
        requestJ.set("remote-port", request.getRemotePort());
        requestJ.set("uri", request.getRequestURI());
        requestJ.set("url", request.getRequestURL());
        requestJ.set("servlet-path", request.getServletPath());
        requestJ.set("method", request.getMethod());
        requestJ.set("query", request.getQueryString());
        requestJ.set("path-info", request.getPathInfo());
        requestJ.set("context-path", request.getContextPath());

        log.info("Request-Info: " + requestJ.toStringPretty());
    }

}