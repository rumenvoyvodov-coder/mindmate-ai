from openai import AsyncOpenAI
from app.core.config import settings
from typing import BinaryIO, AsyncIterable, Dict
import io

class VoiceService:
    def __init__(self):
        self.client = AsyncOpenAI(api_key=settings.OPENAI_API_KEY)
        self.model = "tts-1-hd"  # Using HD model for better quality as requested

    async def transcribe_audio(self, audio_file: BinaryIO) -> str:
        """
        Transcribe audio using OpenAI Whisper API.
        """
        response = await self.client.audio.transcriptions.create(
            model="whisper-1",
            file=audio_file
        )
        return response.text

    def _get_voice_for_request(self, voice_type: str, language: str) -> str:
        """
        Map voice_type and language to an OpenAI voice.
        Therapist: calm, empathetic -> nova, alloy
        Coach: motivating, energetic -> fable, onyx
        """
        # Default mappings
        voice_map = {
            "therapist": "nova",
            "coach": "fable"
        }
        
        # Language specific adjustments if needed
        # OpenAI voices are multilingual but some sound better in certain languages.
        # Alloy and Nova are generally very good for calm tones across languages.
        # Onyx is a deep male voice, good for a 'Coach' in many languages.
        
        if voice_type == "coach":
            # For some languages, onyx might sound more 'authoritative' which fits a coach
            if language in ["en", "de", "ru"]:
                return "onyx"
            return "fable"
        else:
            # Therapist
            if language in ["bg", "ro", "tr"]:
                return "alloy"
            return "nova"

    async def text_to_speech_stream(self, text: str, voice_type: str = "therapist", language: str = "en") -> AsyncIterable[bytes]:
        """
        Convert text to speech and stream the audio using OpenAI TTS API.
        """
        voice = self._get_voice_for_request(voice_type, language)
        
        response = await self.client.audio.speech.create(
            model=self.model,
            voice=voice,
            input=text
        )
        # response is an HttpxBinaryResponseContent which supports async iteration
        async for chunk in response.iter_bytes():
            yield chunk

voice_service = VoiceService()
