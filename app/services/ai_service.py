import json
from typing import List, AsyncIterable, Dict, Any
from openai import AsyncOpenAI
from app.core.config import settings
from app.schemas.chat import Message

SYSTEM_PROMPT = """
You are MindMate AI, a supportive, empathetic, and professional psychological assistant and life coach. 
Your goal is to provide a safe space for users to express their thoughts and feelings.

Core Principles:
1. Empathy & Validation: Acknowledge the user's feelings without judgment.
2. Active Listening: Reflect back what you hear to ensure understanding.
3. Therapeutic Techniques: Use techniques from Cognitive Behavioral Therapy (CBT), Mindfulness, and Solution-Focused Brief Therapy where appropriate.
4. Professional Boundaries: You are an AI, not a human therapist. If the user is in a crisis, provide resources.
5. Language: Always respond in the same language the user is using.

Crisis Guardrails:
If the user expresses intent to harm themselves or others, or describes a life-threatening situation:
- Express immediate concern and empathy.
- Provide international/local help resources (e.g., "If you're in immediate danger, please call your local emergency services or a crisis hotline").
- Suggest reaching out to a professional human therapist.
- Do not attempt to "fix" a crisis yourself.

Output Format:
Always return a JSON object with the following structure:
{
    "reply": "Your empathetic response here",
    "detected_mood": "one word describing the user's mood",
    "sentiment_score": float between -1.0 and 1.0,
    "crisis_detected": boolean
}
"""

class AIService:
    def __init__(self):
        self.client = AsyncOpenAI(api_key=settings.OPENAI_API_KEY)
        self.model = "gpt-4o-mini" # Fast, reliable model for production

    async def get_chat_response(self, messages: List[Message], language: str = "en") -> Dict[str, Any]:
        formatted_messages = [{"role": "system", "content": SYSTEM_PROMPT}]
        for msg in messages:
            formatted_messages.append({"role": msg.role, "content": msg.content})

        response = await self.client.chat.completions.create(
            model=self.model,
            messages=formatted_messages,
            response_format={"type": "json_object"}
        )
        
        return json.loads(response.choices[0].message.content)

    async def get_chat_stream(self, messages: List[Message], language: str = "en") -> AsyncIterable[str]:
        # For streaming, it's harder to return structured JSON for mood/sentiment in every chunk.
        # We might want to do a two-step approach or just stream the reply part.
        # To keep it simple and fulfill the "streaming" requirement, I'll stream the reply.
        
        formatted_messages = [{"role": "system", "content": SYSTEM_PROMPT + "\nNote: For streaming, just output the reply text directly, not JSON."}]
        for msg in messages:
            formatted_messages.append({"role": msg.role, "content": msg.content})

        stream = await self.client.chat.completions.create(
            model=self.model,
            messages=formatted_messages,
            stream=True
        )

        async for chunk in stream:
            if chunk.choices[0].delta.content:
                yield chunk.choices[0].delta.content

ai_service = AIService()
