from fastapi import APIRouter

from app.core.logging import get_request_id
from app.core.result import Result
from app.schemas.chat import ChatCompletionRequest
from app.service.chat_service import chat_service


router = APIRouter()


@router.post("/completions", response_model=Result)
async def completions(request: ChatCompletionRequest) -> Result:
    data = await chat_service.complete(request)
    return Result.ok(data, request_id=get_request_id())
