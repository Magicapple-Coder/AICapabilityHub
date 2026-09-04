package com.aicapabilityhub.gateway.config;

import java.util.UUID;

import com.aicapabilityhub.gateway.filter.AuthGlobalFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * 统一 Sentinel 网关限流响应，避免默认纯文本响应破坏接口契约。
 */
@Configuration(proxyBeanMethods = false)
public class SentinelGatewayConfiguration {

    @Bean
    BlockRequestHandler sentinelBlockRequestHandler() {
        return (exchange, throwable) -> {
            // Sentinel 过滤器早于鉴权过滤器执行，必须在此独立生成可信 requestId。
            String requestId = UUID.randomUUID().toString();
            ErrorResult body = new ErrorResult(4001, "系统繁忙，请稍后重试", null, requestId);
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(AuthGlobalFilter.HEADER_REQUEST_ID, requestId)
                    .bodyValue(body);
        };
    }

    private record ErrorResult(int code, String message, Object data, String requestId) {
    }
}
