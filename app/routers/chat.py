from fastapi import APIRouter, HTTPException, Depends, status
from fastapi.responses import StreamingResponse
from sqlalchemy.orm import Session
from app.schemas.chat import ChatRequest, ChatResponse
from app.services.ai_service import ai_service
from app.services.usage_service import check_usage_limit
from app.core.database import get_db
from app.core.deps import get_current_user
from app.models.user import User
from app.models.conversation import Conversation, Message as DBMessage
from typing import List

router = APIRouter(prefix="/chat", tags=["chat"])

@router.post("/", response_model=ChatResponse)
async def chat_endpoint(
    request: ChatRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    # Check usage limits
    if not check_usage_limit(db, current_user):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Daily message limit reached for free tier. Please upgrade to Premium."
        )

    # Get or create conversation
    if request.conversation_id:
        conversation = db.query(Conversation).filter(
            Conversation.id == request.conversation_id,
            Conversation.user_id == current_user.id
        ).first()
        if not conversation:
            raise HTTPException(status_code=404, detail="Conversation not found")
    else:
        conversation = Conversation(user_id=current_user.id)
        db.add(conversation)
        db.commit()
        db.refresh(conversation)

    # Save user message
    user_msg = DBMessage(
        conversation_id=conversation.id,
        role="user",
        content=request.messages[-1].content
    )
    db.add(user_msg)

    try:
        response_data = await ai_service.get_chat_response(request.messages, request.language)
        
        # Save assistant message
        assistant_msg = DBMessage(
            conversation_id=conversation.id,
            role="assistant",
            content=response_data["reply"],
            mood=response_data.get("detected_mood"),
            sentiment_score=response_data.get("sentiment_score")
        )
        db.add(assistant_msg)
        db.commit()

        return ChatResponse(
            content=response_data["reply"],
            mood=response_data.get("detected_mood"),
            sentiment_score=response_data.get("sentiment_score"),
            language=request.language,
            crisis_detected=response_data.get("crisis_detected", False),
            conversation_id=conversation.id
        )
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=str(e))

# Note: Streaming implementation is more complex with database persistence 
# and mood/sentiment tracking. For the streaming endpoint, we'll keep it 
# simpler or implement a background task to save the results.
# For now, I'll update it to at least require auth.

@router.post("/stream")
async def chat_stream_endpoint(
    request: ChatRequest,
    current_user: User = Depends(get_current_user)
):
    # Just a placeholder for now to enforce auth
    try:
        return StreamingResponse(
            ai_service.get_chat_stream(request.messages, request.language),
            media_type="text/event-stream"
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
