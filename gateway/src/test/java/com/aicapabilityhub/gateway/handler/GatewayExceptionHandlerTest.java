package com.aicapabilityhub.gateway.handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.ConnectException;

import com.aicapabilityhub.gateway.filter.AuthGlobalFilter;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import reactor.test.StepVerifier;

class GatewayExceptionHandlerTest {

    private static final String FORGED_REQUEST_ID = "client-controlled-request-id";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private GatewayExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GatewayExceptionHandler(objectMapper);
    }

    @Test
    void mapsMissingServiceInstanceToUnified503() throws Exception {
        MockServerWebExchange exchange = exchange();

        StepVerifier.create(handler.handle(
                exchange,
                NotFoundException.create(false, "Unable to find instance for user-service")))
                .verifyComplete();

        JsonNode body = responseBody(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(body.get("code").asInt()).isEqualTo(4002);
        assertThat(body.get("message").asText()).isEqualTo("下游服务不可用");
        assertUnifiedBody(exchange, body);
    }

    @Test
    void mapsConnectionFailureToUnified503() throws Exception {
        MockServerWebExchange exchange = exchange();
        String trustedRequestId = "gateway-generated-request-id";
        exchange.getAttributes().put(AuthGlobalFilter.ATTRIBUTE_REQUEST_ID, trustedRequestId);

        StepVerifier.create(handler.handle(exchange, new ConnectException("Connection refused")))
                .verifyComplete();

        JsonNode body = responseBody(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(body.get("code").asInt()).isEqualTo(4002);
        assertUnifiedBody(exchange, body);
        assertThat(body.get("requestId").asText()).isEqualTo(trustedRequestId);
    }

    @Test
    void mapsUnmatchedRouteToUnified404() throws Exception {
        MockServerWebExchange exchange = exchange();

        StepVerifier.create(handler.handle(
                exchange,
                new ResponseStatusException(HttpStatus.NOT_FOUND, "No matching route")))
                .verifyComplete();

        JsonNode body = responseBody(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(body.get("code").asInt()).isEqualTo(4003);
        assertThat(body.get("message").asText()).isEqualTo("请求资源不存在");
        assertUnifiedBody(exchange, body);
    }

    @Test
    void mapsUnexpectedFailureToUnified500() throws Exception {
        MockServerWebExchange exchange = exchange();

        StepVerifier.create(handler.handle(exchange, new IllegalStateException("internal detail")))
                .verifyComplete();

        JsonNode body = responseBody(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(body.get("code").asInt()).isEqualTo(4003);
        assertThat(body.get("message").asText()).isEqualTo("系统内部错误");
        assertUnifiedBody(exchange, body);
    }

    @Test
    void leavesSentinelBlockExceptionForHigherPriorityHandler() {
        MockServerWebExchange exchange = exchange();
        FlowException exception = new FlowException("blocked");

        StepVerifier.create(handler.handle(exchange, exception))
                .expectErrorSatisfies(actual -> assertThat(actual).isSameAs(exception))
                .verify();

        assertThat(exchange.getResponse().isCommitted()).isFalse();
    }

    @Test
    void doesNotWriteResponseAfterItHasBeenCommitted() {
        MockServerWebExchange exchange = exchange();
        IllegalStateException exception = new IllegalStateException("late failure");
        exchange.getResponse().setComplete().block();

        StepVerifier.create(handler.handle(exchange, exception))
                .expectErrorSatisfies(actual -> assertThat(actual).isSameAs(exception))
                .verify();

        assertThat(exchange.getResponse().getBodyAsString().block()).isEmpty();
    }

    private MockServerWebExchange exchange() {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/api/user/ping")
                .header("X-Request-Id", FORGED_REQUEST_ID)
                .build());
    }

    private JsonNode responseBody(MockServerWebExchange exchange) throws Exception {
        return objectMapper.readTree(exchange.getResponse().getBodyAsString().block());
    }

    private void assertUnifiedBody(MockServerWebExchange exchange, JsonNode body) {
        assertThat(body.has("code")).isTrue();
        assertThat(body.has("message")).isTrue();
        assertThat(body.get("data").isNull()).isTrue();
        assertThat(body.get("requestId").asText()).isNotBlank();
        assertThat(body.get("requestId").asText()).isNotEqualTo(FORGED_REQUEST_ID);
        assertThat(exchange.getResponse().getHeaders().getFirst("X-Request-Id"))
                .isEqualTo(body.get("requestId").asText());
    }
}
