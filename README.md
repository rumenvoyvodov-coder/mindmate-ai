# MindMate AI - Backend & Landing Page

MindMate AI is an AI-powered psychological support and coaching platform. This repository contains the FastAPI backend and the marketing landing page.

## 🚀 Features

- **AI Conversation Engine**: Empathetic chat powered by OpenAI with therapeutic prompt engineering.
- **STT/TTS Support**: Integrated Speech-to-Text (Whisper) and Text-to-Speech (OpenAI TTS) endpoints for voice interaction.
- **Multi-language Support**: Seamless communication in over 15 languages.
- **User Management**: Secure JWT-based authentication and profile management.
- **Conversation History**: Persistent storage of chat sessions.
- **Subscription Management**: Freemium model with daily limits for free users and unlimited access for Premium.
- **Responsive Landing Page**: Built with Tailwind CSS, ready for marketing.

## 🛠 Tech Stack

- **Backend**: Python 3.12, FastAPI, SQLAlchemy, Pydantic, SQLite (MVP).
- **AI**: OpenAI GPT-4 API.
- **Auth**: OAuth2 with JWT tokens (passlib, python-jose).
- **Deployment**: Docker, Fly.io / Railway.

## 📁 Project Structure

- `app/`: Main application code
  - `core/`: Configuration, security, constants
  - `models/`: SQLAlchemy database models
  - `schemas/`: Pydantic request/response schemas
  - `routers/`: API endpoints (auth, chat, users, subs)
  - `services/`: AI logic and database operations
  - `static/`: Marketing landing page (`index.html`)
- `tests/`: Integration and unit tests
- `Dockerfile` & `docker-compose.yml`: Containerization
- `fly.toml`: Cloud deployment configuration

## ⚙️ Setup & Installation

### Local Development

1. **Clone & Enter**:
   ```bash
   cd mindmate-backend
   ```

2. **Virtual Environment**:
   ```bash
   python -m venv venv
   source venv/bin/activate
   ```

3. **Install Dependencies**:
   ```bash
   pip install -r requirements.txt
   ```

4. **Environment Variables**:
   Create a `.env` file:
   ```env
   SECRET_KEY="your-secret-key"
   OPENAI_API_KEY="sk-..."
   DATABASE_URL="sqlite:///./mindmate.db"
   ```

5. **Run Server**:
   ```bash
   uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
   ```
   The API will be available at `http://localhost:8000` and docs at `/docs`.

### Docker

```bash
docker-compose up --build
```

## ☁️ Deployment

### Deploy to Fly.io

1. Install Fly CLI and login.
2. Run `fly launch` (it will use the existing `fly.toml`).
3. Set secrets:
   ```bash
   fly secrets set OPENAI_API_KEY=your_key SECRET_KEY=your_key
   ```
4. Deploy:
   ```bash
   fly deploy
   ```

## 🧪 Testing

Run integration tests to verify the full lifecycle:
```bash
python tests/integration_tests.py
```

## 🌍 Landing Page

The marketing landing page is served at the root URL (`/`) of the backend.
It can also be found as a standalone file at `/home/team/shared/mindmate-website/index.html`.
Built with **Tailwind CSS (CDN)** for rapid styling and modern look.

## 📱 Mobile Integration

The backend is designed to work seamlessly with the native iOS and Android apps.
Endpoints:
- `POST /auth/register` - User registration
- `POST /auth/token` - Login / Get JWT
- `GET /chat/sessions` - List conversations
- `POST /chat/stream` - Streaming AI chat
- `POST /voice/stt` - Speech-to-Text (Whisper). Supports all major languages.
- `POST /voice/tts` - Text-to-Speech (OpenAI tts-1-hd).
  - Body: `{ "text": "...", "voice_type": "therapist" | "coach", "language": "bg" | "en" | ... }`
  - Voices are optimized based on `voice_type` and `language`.
- `POST /subscribe/premium` - Upgrade account
