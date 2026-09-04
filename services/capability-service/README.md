# capability-service

能力目录服务的 FastAPI 可运行骨架，独立使用 capability_db；本轮不实现业务 CRUD。

## 启动

1. 将 .env.example 复制为 .env，并按本机环境修改。
2. 执行 python -m pip install -r requirements.txt。
3. 执行 python -m uvicorn app.main:app --host 127.0.0.1 --port 8082。

GET /health 返回统一 Result 且 data.status 为 UP；GET /api/capability/ping 可验证 X-User-Id、X-User-Name、X-Request-Id 透传。Nacos 可用时服务每 5 秒发送心跳；Nacos 暂不可用时服务仍可启动并在后台持续重试。

## 分层约束

按 models、schemas、repository、service、api 分层扩展。服务间调用必须通过 app.core.service_call.call_internal 访问 /internal/**，不得写死其它服务地址。
