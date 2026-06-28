package com.mindmate.ai.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val email: String,
    val name: String,
    @SerializedName("preferred_language") val preferredLanguage: String,
    val tier: String,
    @SerializedName("created_at") val createdAt: String
)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    @SerializedName("preferred_language") val preferredLanguage: String = "English"
)

data class LoginRequest(
    val username: String, // FastAPI OAuth2 expects username (which is email here)
    val password: String
)

data class ChatMessage(
    val role: String,
    val content: String
)

data class ChatRequest(
    val messages: List<ChatMessage>,
    @SerializedName("conversation_id") val conversationId: Int? = null,
    val language: String = "en"
)

data class ChatResponse(
    val content: String,
    @SerializedName("conversation_id") val conversationId: Int,
    val mood: String?,
    @SerializedName("sentiment_score") val sentimentScore: Double?,
    val language: String = "en",
    @SerializedName("crisis_detected") val crisisDetected: Boolean = false
)

data class Conversation(
    val id: Int,
    val title: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class Message(
    val id: Int,
    val role: String,
    val content: String,
    val mood: String?,
    val sentiment: Double?,
    @SerializedName("created_at") val createdAt: String
)

data class UsageStats(
    @SerializedName("daily_count") val dailyCount: Int,
    @SerializedName("daily_limit") val dailyLimit: Int?,
    val remaining: Int?
)

data class TTSRequest(
    val text: String,
    @SerializedName("voice_type") val voiceType: String = "therapist",
    val language: String? = "en"
)

data class STTResponse(
    val text: String
)
