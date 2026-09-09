from fastapi import APIRouter, Depends

from app.core.logging import get_request_id
from app.core.result import Result
from app.core.security import UserContext, current_user
from app.schemas.chat import ChatCompletionRequest
from app.service.chat_service import chat_service


router = APIRouter()


@router.post("/completions", response_model=Result)
async def completions(
    request: ChatCompletionRequest,
    user: UserContext = Depends(current_user),
) -> Result:
    data = await chat_service.complete(request, user)
    return Result.ok(data, request_id=get_request_id())
