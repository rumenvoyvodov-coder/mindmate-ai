from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.core.deps import get_current_user
from app.models.user import User, UserTier

router = APIRouter(prefix="/subscribe", tags=["subscription"])

@router.post("/premium")
def upgrade_to_premium(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    if current_user.tier == UserTier.PREMIUM:
        return {"message": "You are already a Premium user"}
    
    current_user.tier = UserTier.PREMIUM
    db.commit()
    db.refresh(current_user)
    return {"message": "Successfully upgraded to Premium", "tier": current_user.tier}
