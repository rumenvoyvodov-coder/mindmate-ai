package com.mindmate.ai.data.api

import com.mindmate.ai.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface MindMateApi {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<User>

    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") email: String,
        @Field("password") pass: String
    ): Response<LoginResponse>

    @GET("users/me")
    suspend fun getProfile(): Response<User>

    @POST("chat/")
    suspend fun sendMessage(@Body request: ChatRequest): Response<ChatResponse>

    @GET("conversations/")
    suspend fun getConversations(): Response<List<Conversation>>

    @GET("conversations/{id}")
    suspend fun getConversationDetail(@Path("id") id: Int): Response<List<Message>>

    @DELETE("conversations/{id}")
    suspend fun deleteConversation(@Path("id") id: Int): Response<Unit>

    @GET("usage/")
    suspend fun getUsage(): Response<UsageStats>

    @POST("subscribe/premium")
    suspend fun subscribePremium(): Response<User>

    @POST("voice/tts")
    @Streaming
    suspend fun textToSpeech(@Body request: TTSRequest): Response<okhttp3.ResponseBody>

    @Multipart
    @POST("voice/stt")
    suspend fun speechToText(@Part file: okhttp3.MultipartBody.Part): Response<STTResponse>
}
