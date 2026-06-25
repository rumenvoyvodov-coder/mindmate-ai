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

# Rate limiter setup (basic in-memory rate limiter)
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
if not os.path.exists(static_dir):
    os.makedirs(static_dir)
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

@app.get("/privacy")
async def privacy():
    privacy_path = os.path.join(static_dir, "privacy.html")
    if os.path.exists(privacy_path):
        return FileResponse(privacy_path)
    return HTMLResponse("<h1>Privacy Policy</h1><p>Privacy policy content coming soon.</p>")

@app.get("/terms")
async def terms():
    terms_path = os.path.join(static_dir, "terms.html")
    if os.path.exists(terms_path):
        return FileResponse(terms_path)
    return HTMLResponse("<h1>Terms of Service</h1><p>Terms of service content coming soon.</p>")

@app.get(f"{settings.API_V1_STR}/users/me", response_model=UserResponse)
async def read_user_me(current_user: User = Depends(get_current_user)):
    return current_user
