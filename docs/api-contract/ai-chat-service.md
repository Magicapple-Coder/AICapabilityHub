# ai-chat-service 接口契约

所有响应均为 `Result`：`{code, message, data, requestId}`；`requestId` 为唯一驼峰字段，其余业务字段使用 snake_case。

## `POST /api/chat/completions`

需要网关 JWT。请求体兼容 OpenAI Chat Completions：

```json
{
  "session_id": 1,
  "model": "mock-model",
  "messages": [{"role": "user", "content": "你好"}],
  "temperature": 0.7,
  "stream": false
}
```

`USE_MOCK=true` 时返回固定 Mock 回复，不访问外部模型；`USE_MOCK=false` 在当前阶段返回 `5002`（真实模型待接入）。返回 `data` 为 OpenAI 风格的 `chat.completion` 对象。`ENABLE_INTERNAL_CHAIN=true` 时，服务先调用 `user-service` 的 `/internal/user/check`，成功后执行能力，再向 `billing-service` 的 `/internal/billing/record` 上报；上报请求使用当前 `X-Request-Id`，由计费服务保证幂等。

## `GET /api/chat/sessions`

需要 JWT。按当前用户返回会话列表：`data={"list":[...],"total":0,"page":1,"page_size":20}`，`page_size` 最大 100。查询默认过滤 `is_deleted=1`。

## `GET /api/chat/sessions/{session_id}/messages`

需要 JWT，只能读取当前用户的会话。会话不存在或不属于当前用户时返回业务错误 `4004`，HTTP 状态仍为 200。

## `GET /health`、`GET /api/chat/ping`

健康检查白名单放行；`ping` 用于验证网关注入的 `X-User-Id`、`X-User-Name`、`X-Request-Id`。

## 配置

`ENABLE_INTERNAL_CHAIN` 默认 `false`，便于仅启动 ai-chat 做 Mock 骨架自测；联调 user/billing 时设为 `true`。服务发现地址、模型开关和密钥均来自环境变量或 Nacos，禁止提交真实 `.env`。
