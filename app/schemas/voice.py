from pydantic import BaseModel, Field
from typing import Optional, Literal

class TTSRequest(BaseModel):
    text: str = Field(..., description="The text to convert to speech")
    voice_type: Literal["therapist", "coach"] = Field("therapist", description="The type of voice to use")
    language: Optional[str] = Field("en", description="ISO language code (e.g., 'en', 'bg', 'es')")
