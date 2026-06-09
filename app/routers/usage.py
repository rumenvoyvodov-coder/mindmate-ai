from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.deps import get_current_user
from app.models.user import User, UserTier
from app.services.usage_service import get_message_count_today

router = APIRouter(prefix="/usage", tags=["usage"])

@router.get("/")
def get_usage(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    count = get_message_count_today(db, current_user.id)
    limit = 10 if current_user.tier == UserTier.FREE else None
    return {
        "messages_sent_today": count,
        "daily_limit": limit,
        "remaining": max(0, limit - count) if limit is not None else None,
        "tier": current_user.tier
    }
