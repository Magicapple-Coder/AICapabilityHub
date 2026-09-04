package com.aicapabilityhub.gateway.handler;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import com.aicapabilityhub.gateway.filter.AuthGlobalFilter;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

/**
 * 将网关路由与系统异常转换为平台统一响应。
 *
 * <p>Sentinel 的异常处理器使用最高优先级，本处理器紧随其后；鉴权过滤器直接写响应，
 * 不会进入此异常链。响应一旦提交则继续传播原异常，避免重复写入。</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public final class GatewayExceptionHandler implements WebExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayExceptionHandler.class);
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final ObjectMapper objectMapper;

    public GatewayExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable exception) {
        if (exchange.getResponse().isCommitted() || BlockException.isBlockException(exception)) {
            return Mono.error(exception);
        }

        ErrorMapping mapping = mapException(exception);
        String requestId = resolveRequestId(exchange);
        byte[] body = serialize(new ErrorResult(
                mapping.code(), mapping.message(), null, requestId));

        LOGGER.warn(
                "requestId={} userId=anonymous serviceName=api-gateway method={} path={} status={} exceptionType={}",
                requestId,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI().getPath(),
                mapping.status().value(),
                exception.getClass().getName());

        exchange.getResponse().setStatusCode(mapping.status());
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().getHeaders().set(REQUEST_ID_HEADER, requestId);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private ErrorMapping mapException(Throwable exception) {
        if (exception instanceof NotFoundException || isDownstreamConnectionFailure(exception)) {
            return new ErrorMapping(HttpStatus.SERVICE_UNAVAILABLE, 4002, "下游服务不可用");
        }

        ResponseStatusException statusException = findCause(exception, ResponseStatusException.class);
        if (statusException != null) {
            HttpStatusCode status = statusException.getStatusCode();
            if (isDownstreamStatus(status)) {
                return new ErrorMapping(HttpStatus.SERVICE_UNAVAILABLE, 4002, "下游服务不可用");
            }
            if (status.value() == HttpStatus.NOT_FOUND.value()) {
                return new ErrorMapping(HttpStatus.NOT_FOUND, 4003, "请求资源不存在");
            }
            if (status.value() == HttpStatus.METHOD_NOT_ALLOWED.value()) {
                return new ErrorMapping(HttpStatus.METHOD_NOT_ALLOWED, 4003, "请求方法不支持");
            }
            return new ErrorMapping(status, 4003, "请求失败");
        }

        return new ErrorMapping(HttpStatus.INTERNAL_SERVER_ERROR, 4003, "系统内部错误");
    }

    private String resolveRequestId(ServerWebExchange exchange) {
        Object trusted = exchange.getAttribute(AuthGlobalFilter.ATTRIBUTE_REQUEST_ID);
        if (trusted instanceof String requestId && !requestId.isBlank()) {
            return requestId;
        }
        return UUID.randomUUID().toString();
    }

    private boolean isDownstreamConnectionFailure(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof IOException
                    || current instanceof TimeoutException
                    || current instanceof UnknownHostException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isDownstreamStatus(HttpStatusCode status) {
        return status.value() == HttpStatus.BAD_GATEWAY.value()
                || status.value() == HttpStatus.SERVICE_UNAVAILABLE.value()
                || status.value() == HttpStatus.GATEWAY_TIMEOUT.value();
    }

    private <T extends Throwable> T findCause(Throwable exception, Class<T> expectedType) {
        Throwable current = exception;
        while (current != null) {
            if (expectedType.isInstance(current)) {
                return expectedType.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }

    private byte[] serialize(ErrorResult result) {
        try {
            return objectMapper.writeValueAsBytes(result);
        } catch (JsonProcessingException exception) {
            return ("{\"code\":" + result.code()
                    + ",\"message\":\"" + result.message()
                    + "\",\"data\":null,\"requestId\":\"" + result.requestId() + "\"}")
                    .getBytes(StandardCharsets.UTF_8);
        }
    }

    private record ErrorMapping(HttpStatusCode status, int code, String message) {
    }

    private record ErrorResult(int code, String message, Object data, String requestId) {
    }
}
