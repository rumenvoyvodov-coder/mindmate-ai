package com.mindmate.ai.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindmate.ai.data.model.Conversation
import com.mindmate.ai.data.model.Message
import com.mindmate.ai.data.model.UsageStats
import com.mindmate.ai.data.model.User
import com.mindmate.ai.data.repository.ChatRepository
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {
    var messages = mutableStateListOf<Message>()
    var conversations = mutableStateListOf<Conversation>()
    var usageStats by mutableStateOf<UsageStats?>(null)
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var conversationId by mutableStateOf<Int?>(null)
    
    // Voice State
    var isListening by mutableStateOf(false)
    var speechResult by mutableStateOf("")
    var ttsEnabled by mutableStateOf(true)
    var voiceType by mutableStateOf("therapist") // "therapist" or "coach"
    var useBackendVoice by mutableStateOf(false) // Local STT is more reliable for now

    fun sendMessage(content: String, onResponse: (String) -> Unit = {}, onStreamReady: (okhttp3.ResponseBody) -> Unit = {}) {
        if (content.isBlank()) return

        val userMessage = Message(
            id = 0,
            role = "user",
            content = content,
            mood = null,
            sentiment = null,
            createdAt = ""
        )
        messages.add(userMessage)

        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val response = repository.sendMessage(content, conversationId)
                if (response.isSuccessful) {
                    val chatResponse = response.body()
                    if (chatResponse != null) {
                        conversationId = chatResponse.conversationId
                        messages.add(
                            Message(
                                id = 0,
                                role = "assistant",
                                content = chatResponse.content,
                                mood = chatResponse.mood,
                                sentiment = chatResponse.sentimentScore,
                                createdAt = ""
                            )
                        )
                        loadUsage()
                        
                        if (ttsEnabled) {
                            if (useBackendVoice) {
                                // Default to user preferred language if possible, otherwise null (backend defaults to "en")
                                val ttsResponse = repository.textToSpeech(chatResponse.content, voiceType, null)
                                if (ttsResponse.isSuccessful) {
                                    ttsResponse.body()?.let { onStreamReady(it) }
                                } else {
                                    onResponse(chatResponse.content) // Fallback to local
                                }
                            } else {
                                onResponse(chatResponse.content)
                            }
                        }
                    }
                } else {
                    error = "Failed to send message: ${response.message()}"
                }
            } catch (e: Exception) {
                error = e.message ?: "An unknown error occurred"
            } finally {
                isLoading = false
            }
        }
    }

    fun transcribeAudio(file: java.io.File, onResult: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                val requestFile = file.asRequestBody("audio/m4a".toMediaTypeOrNull())
                val body = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestFile)
                val response = repository.speechToText(body)
                if (response.isSuccessful) {
                    response.body()?.let { onResult(it.text) }
                } else {
                    error = "STT failed: ${response.message()}"
                }
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun loadConversations() {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val response = repository.getConversations()
                if (response.isSuccessful) {
                    conversations.clear()
                    response.body()?.let { conversations.addAll(it) }
                } else {
                    error = "Failed to load conversations: ${response.message()}"
                }
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun loadConversation(id: Int) {
        conversationId = id
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val response = repository.getConversationDetail(id)
                if (response.isSuccessful) {
                    messages.clear()
                    response.body()?.let { messages.addAll(it) }
                } else {
                    error = "Failed to load messages: ${response.message()}"
                }
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteConversation(id: Int) {
        viewModelScope.launch {
            try {
                val response = repository.deleteConversation(id)
                if (response.isSuccessful) {
                    conversations.removeIf { it.id == id }
                    if (conversationId == id) {
                        startNewChat()
                    }
                }
            } catch (e: Exception) {
                error = e.message
            }
        }
    }

    fun loadUsage() {
        viewModelScope.launch {
            try {
                val response = repository.getUsage()
                if (response.isSuccessful) {
                    usageStats = response.body()
                }
            } catch (e: Exception) {
                error = e.message
            }
        }
    }

    fun subscribePremium(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = repository.subscribePremium()
                if (response.isSuccessful) {
                    loadUsage()
                    onSuccess()
                }
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun startNewChat() {
        conversationId = null
        messages.clear()
    }
}
