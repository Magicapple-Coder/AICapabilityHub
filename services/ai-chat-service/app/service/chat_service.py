import time
from uuid import uuid4

from app.core.config import model_runtime_config
from app.core.config import settings
from app.core.exceptions import BizException
from app.core.logging import get_request_id
from app.core.security import UserContext
from app.core.service_call import call_internal
from app.schemas.chat import ChatCompletionRequest


MOCK_REPLY = "你好！这是 ai-chat-service 的 Mock 回复。"


class ChatService:
    """对话能力骨架；Mock 分支不会访问任何外部模型。"""

    async def complete(self, request: ChatCompletionRequest, user: UserContext) -> dict:
        runtime_config = model_runtime_config.snapshot()
        if settings.enable_internal_chain:
            if not user.user_id:
                raise BizException(1001, "未认证或登录已过期")
            await call_internal(
                settings.user_service_name,
                "GET",
                "/internal/user/check",
                params={"user_id": user.user_id, "points_cost": settings.points_cost},
            )
        if not runtime_config.use_mock:
            # 本轮禁止接真实模型，显式返回预留错误码。
            raise BizException(5002, "真实模型尚未接入，请将 USE_MOCK=true 后重试")

        prompt_tokens = sum(len(message.content) for message in request.messages)
        completion_tokens = len(MOCK_REPLY)
        result = {
            "id": f"chatcmpl-{uuid4().hex}",
            "object": "chat.completion",
            "created": int(time.time()),
            "model": request.model,
            "choices": [
                {
                    "index": 0,
                    "message": {"role": "assistant", "content": MOCK_REPLY},
                    "finish_reason": "stop",
                }
            ],
            "usage": {
                "prompt_tokens": prompt_tokens,
                "completion_tokens": completion_tokens,
                "total_tokens": prompt_tokens + completion_tokens,
            },
            "mock": True,
        }
        if settings.enable_internal_chain:
            await call_internal(
                settings.billing_service_name,
                "POST",
                "/internal/billing/record",
                json={
                    "request_id": get_request_id(),
                    "user_id": user.user_id,
                    "service_name": settings.service_name,
                    "model": request.model,
                    "points": settings.points_cost,
                    "status": "success",
                },
            )
        return result


chat_service = ChatService()
