package com.mindmate.ai.data.repository

import com.mindmate.ai.data.api.MindMateApi
import com.mindmate.ai.data.model.*
import retrofit2.Response

class AuthRepository(
    private val api: MindMateApi,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String): Response<LoginResponse> {
        val response = api.login(email, password)
        if (response.isSuccessful) {
            response.body()?.accessToken?.let {
                tokenManager.saveToken(it)
            }
        }
        return response
    }

    suspend fun register(request: RegisterRequest): Response<User> {
        return api.register(request)
    }

    suspend fun getProfile(): Response<User> {
        return api.getProfile()
    }

    suspend fun logout() {
        tokenManager.clearToken()
    }
}
