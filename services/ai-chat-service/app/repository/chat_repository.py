from sqlalchemy import desc, func, select
from sqlalchemy.orm import Session

from app.models.chat import ChatMessage, ChatSession


class ChatRepository:
    """只负责 ai_chat_db 的查询和持久化，不跨库访问其它服务。"""

    def list_sessions(
        self, db: Session, user_id: str, *, page: int = 1, page_size: int = 20
    ) -> tuple[list[ChatSession], int]:
        predicate = ChatSession.user_id == user_id, ChatSession.is_deleted == 0
        statement = (
            select(ChatSession)
            .where(*predicate)
            .order_by(desc(ChatSession.update_time), desc(ChatSession.id))
            .offset((page - 1) * page_size)
            .limit(page_size)
        )
        count_statement = select(func.count(ChatSession.id)).where(*predicate)
        total = db.scalar(count_statement) or 0
        return list(db.scalars(statement).all()), total

    def find_session(self, db: Session, user_id: str, session_id: int) -> ChatSession | None:
        statement = select(ChatSession).where(
            ChatSession.id == session_id,
            ChatSession.user_id == user_id,
            ChatSession.is_deleted == 0,
        )
        return db.scalar(statement)

    def list_messages(self, db: Session, session: ChatSession) -> list[ChatMessage]:
        statement = (
            select(ChatMessage)
            .where(
                ChatMessage.session_id == session.id,
                ChatMessage.user_id == session.user_id,
                ChatMessage.is_deleted == 0,
            )
            .order_by(ChatMessage.id)
        )
        return list(db.scalars(statement).all())


chat_repository = ChatRepository()
