from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from loguru import logger
from starlette.exceptions import HTTPException as StarletteHTTPException

from app.core.logging import get_request_id
from app.core.result import Result


class BizException(Exception):
    def __init__(self, code: int, message: str):
        super().__init__(message)
        self.code = code
        self.message = message


def _request_id(request: Request) -> str:
    return getattr(request.state, "request_id", "") or get_request_id()


def _response(result: Result) -> JSONResponse:
    return JSONResponse(
        status_code=200,
        content=result.model_dump(mode="json", by_alias=True),
    )


def register_exception_handlers(app: FastAPI) -> None:
    @app.exception_handler(BizException)
    async def biz_exception_handler(request: Request, exc: BizException) -> JSONResponse:
        logger.warning("业务异常 code={}", exc.code)
        return _response(Result.fail(exc.code, exc.message, _request_id(request)))

    @app.exception_handler(RequestValidationError)
    async def validation_exception_handler(
        request: Request, exc: RequestValidationError
    ) -> JSONResponse:
        # 只记录错误数量，避免非法输入中的密码或密钥进入日志。
        logger.warning("请求参数校验失败 count={}", len(exc.errors()))
        return _response(Result.fail(2001, "参数不符合 Schema", _request_id(request)))

    @app.exception_handler(StarletteHTTPException)
    async def http_exception_handler(request: Request, exc: StarletteHTTPException) -> JSONResponse:
        # 路由不存在或方法不支持时也保持统一响应结构。
        message = "请求资源不存在" if exc.status_code == 404 else "请求方法不支持" if exc.status_code == 405 else "请求失败"
        return _response(Result.fail(4003, message, _request_id(request)))

    @app.exception_handler(Exception)
    async def unknown_exception_handler(request: Request, exc: Exception) -> JSONResponse:
        logger.error("未处理的系统异常 type={}", type(exc).__name__)
        return _response(Result.fail(4003, "系统内部错误", _request_id(request)))
