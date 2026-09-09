from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field


class ChatSessionResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    title: str
    create_time: datetime
    update_time: datetime


class ChatMessageResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    session_id: int
    role: str
    content: str
    create_time: datetime
