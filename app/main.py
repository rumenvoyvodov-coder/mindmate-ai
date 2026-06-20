from fastapi import FastAPI, Depends, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from fastapi.responses import HTMLResponse, FileResponse
from app.core.config import settings
from app.routers import chat, auth, conversations, usage, subscription, voice
from app.core.database import engine, Base
from app.core.deps import get_current_user
from app.models.user import User
from app.models.conversation import Conversation, Message
from app.schemas.user import UserResponse
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded
from starlette.middleware.base import BaseHTTPMiddleware
import os

# Create database tables
Base.metadata.create_all(bind=engine)

# Rate limiter setup
limiter = Limiter(key_func=get_remote_address)
app = FastAPI(
    title=settings.PROJECT_NAME,
    openapi_url=f"{settings.API_V1_STR}/openapi.json"
)
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

# Security Headers Middleware
class SecurityHeadersMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        response = await call_next(request)
        response.headers["X-Content-Type-Options"] = "nosniff"
        response.headers["X-Frame-Options"] = "DENY"
        response.headers["Strict-Transport-Security"] = "max-age=31536000; includeSubDomains"
        response.headers["Content-Security-Policy"] = "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; connect-src 'self' *.openai.com"
        return response

app.add_middleware(SecurityHeadersMiddleware)

# Set CORS origins
if settings.BACKEND_CORS_ORIGINS:
    app.add_middleware(
        CORSMiddleware,
        allow_origins=[str(origin) for origin in settings.BACKEND_CORS_ORIGINS] or ["*"],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

app.include_router(chat.router, prefix=settings.API_V1_STR)
app.include_router(auth.router, prefix=settings.API_V1_STR)
app.include_router(conversations.router, prefix=settings.API_V1_STR)
app.include_router(usage.router, prefix=settings.API_V1_STR)
app.include_router(subscription.router, prefix=settings.API_V1_STR)
app.include_router(voice.router, prefix=settings.API_V1_STR)

# Mount static files
static_dir = os.path.join(os.path.dirname(__file__), "static")
if os.path.exists(static_dir):
    app.mount("/static", StaticFiles(directory=static_dir), name="static")

@app.get("/")
@limiter.limit("5/minute")
async def root(request: Request):
    index_path = os.path.join(static_dir, "index.html")
    if os.path.exists(index_path):
        return FileResponse(index_path)
    return {"message": "Welcome to MindMate AI API"}

@app.get("/health")
async def health_check():
    return {"status": "healthy"}

@app.get("/privacy", response_class=HTMLResponse)
async def privacy_policy(request: Request, lang: str = "en"):
    if lang == "bg":
        return """
        <html>
            <head><title>Политика за поверителност - MindMate AI</title></head>
            <body>
                <h1>Политика за поверителност</h1>
                <p>Вашата поверителност е важна за нас. MindMate AI не споделя вашите лични данни с трети страни.</p>
                <p>Всички разговори са анонимни и криптирани.</p>
            </body>
        </html>
        """
    return """
    <html>
        <head><title>Privacy Policy - MindMate AI</title></head>
        <body>
            <h1>Privacy Policy</h1>
            <p>Your privacy is important to us. MindMate AI does not share your personal data with third parties.</p>
            <p>All conversations are anonymous and encrypted.</p>
        </body>
    </html>
    """

@app.get("/terms", response_class=HTMLResponse)
async def terms_of_service(request: Request, lang: str = "en"):
    if lang == "bg":
        return """
        <html>
            <head><title>Условия за ползване - MindMate AI</title></head>
            <body>
                <h1>Условия за ползване</h1>
                <p>MindMate AI е приложение за психологическа подкрепа, базирано на изкуствен интелект.</p>
                <p>То не заменя професионалната медицинска помощ.</p>
            </body>
        </html>
        """
    return """
    <html>
        <head><title>Terms of Service - MindMate AI</title></head>
        <body>
            <h1>Terms of Service</h1>
            <p>MindMate AI is an AI-based psychological support application.</p>
            <p>It does not replace professional medical help.</p>
        </body>
    </html>
    """

@app.get(f"{settings.API_V1_STR}/users/me", response_model=UserResponse)
async def read_user_me(current_user: User = Depends(get_current_user)):
    return current_user
