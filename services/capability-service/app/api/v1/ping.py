from fastapi import APIRouter, Depends

from app.core.logging import get_request_id
from app.core.result import Result
from app.core.security import UserContext, current_user


router = APIRouter()


@router.get("/ping", response_model=Result)
async def ping(user: UserContext = Depends(current_user)) -> Result:
    """占位接口：验证网关路由与身份透传。"""

    return Result.ok(
        {
            "service_name": "capability-service",
            "domain": "capability",
            "message": "pong",
            "user_id": user.user_id,
            "user_name": user.user_name,
        },
        request_id=user.request_id or get_request_id(),
    )
