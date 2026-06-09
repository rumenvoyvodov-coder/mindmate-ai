from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any

class Message(BaseModel):
    role: str = Field(..., description="Role of the message sender (user, assistant, system)")
    content: str = Field(..., description="Content of the message")

class ChatRequest(BaseModel):
    messages: List[Message]
    conversation_id: Optional[int] = None
    language: Optional[str] = "en"

class ChatResponse(BaseModel):
    content: str
    mood: Optional[str] = None
    sentiment_score: Optional[float] = None
    language: str
    crisis_detected: bool = False
    conversation_id: int
