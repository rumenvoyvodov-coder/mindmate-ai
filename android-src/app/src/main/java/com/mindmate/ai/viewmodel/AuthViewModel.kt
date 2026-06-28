package com.mindmate.ai.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindmate.ai.data.model.RegisterRequest
import com.mindmate.ai.data.model.User
import com.mindmate.ai.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var loginSuccess by mutableStateOf(false)
    var registerSuccess by mutableStateOf(false)
    var user by mutableStateOf<User?>(null)

    fun login(email: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val response = repository.login(email, password)
                if (response.isSuccessful) {
                    loginSuccess = true
                    loadProfile()
                } else {
                    error = "Login failed: ${response.message()}"
                }
            } catch (e: Exception) {
                error = e.message ?: "An unknown error occurred"
            } finally {
                isLoading = false
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val response = repository.register(RegisterRequest(email, password, name))
                if (response.isSuccessful) {
                    registerSuccess = true
                } else {
                    error = "Registration failed: ${response.message()}"
                }
            } catch (e: Exception) {
                error = e.message ?: "An unknown error occurred"
            } finally {
                isLoading = false
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = repository.getProfile()
                if (response.isSuccessful) {
                    user = response.body()
                }
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            user = null
            loginSuccess = false
        }
    }
}
