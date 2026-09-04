from typing import Any

from pydantic import BaseModel, ConfigDict, Field


class Result(BaseModel):
    """平台统一响应；Python 内部用蛇形命名，对外保留 requestId。"""

    model_config = ConfigDict(populate_by_name=True, serialize_by_alias=True)

    code: int = 0
    message: str = "success"
    data: Any | None = None
    request_id: str = Field(default="", alias="requestId")

    @classmethod
    def ok(
        cls,
        data: Any = None,
        message: str = "success",
        request_id: str = "",
    ) -> "Result":
        return cls(code=0, message=message, data=data, request_id=request_id)

    @classmethod
    def fail(cls, code: int, message: str, request_id: str = "") -> "Result":
        return cls(code=code, message=message, data=None, request_id=request_id)
