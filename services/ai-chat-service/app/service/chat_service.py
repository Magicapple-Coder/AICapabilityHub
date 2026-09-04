import time
from uuid import uuid4

from app.core.config import model_runtime_config
from app.core.exceptions import BizException
from app.schemas.chat import ChatCompletionRequest


MOCK_REPLY = "你好！这是 ai-chat-service 的 Mock 回复。"


class ChatService:
    """对话能力骨架；Mock 分支不会访问任何外部模型。"""

    async def complete(self, request: ChatCompletionRequest) -> dict:
        runtime_config = model_runtime_config.snapshot()
        if not runtime_config.use_mock:
            # 本轮禁止接真实模型，显式返回预留错误码。
            raise BizException(5002, "真实模型尚未接入，请将 USE_MOCK=true 后重试")

        prompt_tokens = sum(len(message.content) for message in request.messages)
        completion_tokens = len(MOCK_REPLY)
        return {
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


chat_service = ChatService()
