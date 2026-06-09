from pydantic_settings import BaseSettings
from typing import Optional

class Settings(BaseSettings):
    PROJECT_NAME: str = "MindMate AI"
    API_V1_STR: str = "/api/v1"
    SECRET_KEY: str = "development_secret_key"  # Change in production
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 8  # 8 days
    
    OPENAI_API_KEY: Optional[str] = None
    
    # Database
    DATABASE_URL: str = "sqlite:///./mindmate.db"

    class Config:
        env_file = ".env"
        case_sensitive = True

settings = Settings()
