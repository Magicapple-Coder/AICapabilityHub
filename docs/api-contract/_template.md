# 接口契约：服务名

> 负责人：姓名  
> 服务名：`example-service`  
> 数据库：`example_db`

### METHOD /api/domain/resource

- 负责人：姓名
- 是否需要登录：是 / 否（白名单）
- 请求头：`Authorization: Bearer <JWT>`（需要登录时）
- 路径参数：无
- 查询参数：无
- 请求体：`{ "field_name": "字段说明" }`
- 成功 data：`{ "field_name": "字段说明" }`
- 错误码：`2001` 参数不符合 Schema
- 备注：补充幂等、分页或服务调用约束

统一成功响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "requestId": "b3f1...c2"
}
```

统一业务错误响应仍使用 HTTP 200，通过 `code` 区分；网关认证失败使用 HTTP 401。所有响应均须包含 `code`、`message`、`data`、`requestId`。
