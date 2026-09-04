package com.aicapabilityhub.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.aicapabilityhub.gateway.filter.AuthGlobalFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

class SentinelGatewayConfigurationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void returnsUnifiedResultWhenSentinelBlocksRequest() throws Exception {
        BlockRequestHandler previous = GatewayCallbackManager.getBlockHandler();
        BlockRequestHandler configured = new SentinelGatewayConfiguration().sentinelBlockRequestHandler();
        GatewayCallbackManager.setBlockHandler(configured);

        try {
            SentinelGatewayBlockExceptionHandler exceptionHandler =
                    new SentinelGatewayBlockExceptionHandler(List.of(), ServerCodecConfigurer.create());
            MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest
                    .get("/api/chat/ping")
                    .header(AuthGlobalFilter.HEADER_REQUEST_ID, "client-controlled-request-id")
                    .build());

            StepVerifier.create(exceptionHandler.handle(exchange, new FlowException("test")))
                    .verifyComplete();

            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
            assertThat(exchange.getResponse().getHeaders().getContentType())
                    .isEqualTo(MediaType.APPLICATION_JSON);

            JsonNode body = objectMapper.readTree(exchange.getResponse().getBodyAsString().block());
            String requestId = exchange.getResponse().getHeaders()
                    .getFirst(AuthGlobalFilter.HEADER_REQUEST_ID);
            assertThat(body.get("code").asInt()).isEqualTo(4001);
            assertThat(body.get("message").asText()).isEqualTo("系统繁忙，请稍后重试");
            assertThat(body.get("data").isNull()).isTrue();
            assertThat(body.get("requestId").asText()).isEqualTo(requestId);
            assertThat(requestId).isNotBlank().isNotEqualTo("client-controlled-request-id");
        } finally {
            GatewayCallbackManager.setBlockHandler(previous);
        }
    }
}
