from fastapi import APIRouter, Depends, UploadFile, File, HTTPException
from fastapi.responses import StreamingResponse
from app.services.voice_service import voice_service
from app.core.deps import get_current_user
from app.models.user import User
from app.schemas.voice import TTSRequest
import io

router = APIRouter(prefix="/voice", tags=["voice"])

@router.post("/stt")
async def speech_to_text(
    file: UploadFile = File(...),
    current_user: User = Depends(get_current_user)
):
    # Allowed formats for Whisper: mp3, mp4, mpeg, mpga, m4a, wav, webm
    allowed_extensions = {".mp3", ".mp4", ".mpeg", ".mpga", ".m4a", ".wav", ".webm"}
    file_ext = "." + file.filename.split(".")[-1].lower() if "." in file.filename else ""
    
    if file_ext not in allowed_extensions:
         raise HTTPException(
            status_code=400, 
            detail=f"Unsupported file format. Allowed formats: {', '.join(allowed_extensions)}"
        )

    try:
        content = await file.read()
        audio_file = io.BytesIO(content)
        audio_file.name = file.filename
        
        text = await voice_service.transcribe_audio(audio_file)
        return {"text": text}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/tts")
async def text_to_speech(
    request: TTSRequest,
    current_user: User = Depends(get_current_user)
):
    try:
        return StreamingResponse(
            voice_service.text_to_speech_stream(
                text=request.text, 
                voice_type=request.voice_type, 
                language=request.language
            ),
            media_type="audio/mpeg"
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
