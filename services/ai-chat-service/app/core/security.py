from fastapi import Header
from pydantic import BaseModel


class UserContext(BaseModel):
    user_id: str = ""
    user_name: str = ""
    request_id: str = ""


def current_user(
    x_user_id: str = Header(default="", alias="X-User-Id"),
    x_user_name: str = Header(default="", alias="X-User-Name"),
    x_request_id: str = Header(default="", alias="X-Request-Id"),
) -> UserContext:
    """读取网关注入的身份，不在业务服务内重复解析 JWT。"""

    return UserContext(
        user_id=x_user_id,
        user_name=x_user_name,
        request_id=x_request_id,
    )
