"""Pydantic 请求与响应模型；模板暂不包含业务 Schema。"""
# TODO: 按接口契约补充本服务的 Pydantic Schema。
from app.schemas.session import ChatMessageResponse, ChatSessionResponse

__all__ = ["ChatMessageResponse", "ChatSessionResponse"]
