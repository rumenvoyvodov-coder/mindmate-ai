# MindMate AI Android App

A native Android application for MindMate AI, built with Kotlin and Jetpack Compose.

## Tech Stack
- **UI**: Jetpack Compose with Material 3
- **Networking**: Retrofit + OkHttp
- **Data Persistence**: Jetpack DataStore (for tokens)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Asynchronous Work**: Kotlin Coroutines + Flow
- **Background Tasks**: WorkManager (for reminders)
- **Push Notifications**: Firebase Cloud Messaging (FCM)

## Project Structure
- `data/api/`: Retrofit interfaces and interceptors
- `data/model/`: Data models (POJOs)
- `data/repository/`: Repositories for data abstraction
- `ui/screens/`: Compose screens
- `ui/theme/`: Material 3 theme and colors
- `viewmodel/`: ViewModels for state management
- `service/`: FCM and WorkManager services

## Setup
1. Ensure the backend is running at `http://localhost:8000`.
2. The app is configured to connect to `http://10.0.2.2:8000` (standard Android emulator loopback).
3. A `google-services.json` file is required for Firebase functionality.

## Features
- User Authentication (Login/Register)
- Real-time Chat with AI
- Conversation History Management
- Daily Mood/Sentiment Tracking (UI)
- Premium Subscription Upgrade
- Push Notifications and Daily Reminders
