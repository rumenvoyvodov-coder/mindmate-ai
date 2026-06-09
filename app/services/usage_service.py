from sqlalchemy.orm import Session
from sqlalchemy import func
from datetime import datetime, time
from app.models.conversation import Message, Conversation
from app.models.user import User, UserTier

def get_message_count_today(db: Session, user_id: int) -> int:
    today_start = datetime.combine(datetime.utcnow().date(), time.min)
    return db.query(Message).join(Conversation).filter(
        Conversation.user_id == user_id,
        Message.created_at >= today_start,
        Message.role == "user"
    ).count()

def check_usage_limit(db: Session, user: User) -> bool:
    if user.tier == UserTier.PREMIUM:
        return True
    
    count = get_message_count_today(db, user.id)
    return count < 10
