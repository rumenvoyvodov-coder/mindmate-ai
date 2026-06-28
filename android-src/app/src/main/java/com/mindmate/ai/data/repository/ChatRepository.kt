package com.mindmate.ai.data.repository

import com.mindmate.ai.data.api.MindMateApi
import com.mindmate.ai.data.model.*
import retrofit2.Response

class ChatRepository(private val api: MindMateApi) {
    suspend fun sendMessage(message: String, conversationId: Int?): Response<ChatResponse> {
        return api.sendMessage(ChatRequest(
            messages = listOf(ChatMessage(role = "user", content = message)),
            conversationId = conversationId
        ))
    }

    suspend fun getConversations(): Response<List<Conversation>> {
        return api.getConversations()
    }

    suspend fun getConversationDetail(id: Int): Response<List<Message>> {
        return api.getConversationDetail(id)
    }

    suspend fun deleteConversation(id: Int): Response<Unit> {
        return api.deleteConversation(id)
    }

    suspend fun getUsage(): Response<UsageStats> {
        return api.getUsage()
    }

    suspend fun subscribePremium(): Response<User> {
        return api.subscribePremium()
    }

    suspend fun textToSpeech(text: String, voiceType: String, language: String?): Response<okhttp3.ResponseBody> {
        return api.textToSpeech(TTSRequest(text, voiceType, language))
    }

    suspend fun speechToText(filePart: okhttp3.MultipartBody.Part): Response<STTResponse> {
        return api.speechToText(filePart)
    }
}
