package com.mindmate.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mindmate.ai.ui.theme.MindMateTheme
import com.mindmate.ai.ui.screens.*

import com.mindmate.ai.data.api.RetrofitClient
import com.mindmate.ai.data.repository.AuthRepository
import com.mindmate.ai.data.repository.ChatRepository
import com.mindmate.ai.data.repository.TokenManager
import com.mindmate.ai.viewmodel.AuthViewModel
import com.mindmate.ai.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val tokenManager = TokenManager(applicationContext)
        val api = RetrofitClient.create(tokenManager)
        val authRepository = AuthRepository(api, tokenManager)
        val chatRepository = ChatRepository(api)
        
        val authViewModel = AuthViewModel(authRepository)
        val chatViewModel = ChatViewModel(chatRepository)

        setContent {
            MindMateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MindMateApp(authViewModel, chatViewModel)
                }
            }
        }
    }
}

@Composable
fun MindMateApp(authViewModel: AuthViewModel, chatViewModel: ChatViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController, authViewModel) }
        composable("register") { RegisterScreen(navController, authViewModel) }
        composable("chat") { ChatScreen(navController, chatViewModel, authViewModel) }
        composable("conversations") { ConversationsScreen(navController, chatViewModel) }
        composable("settings") { SettingsScreen(navController, authViewModel, chatViewModel) }
        composable("premium") { PremiumScreen(navController, chatViewModel) }
        composable("privacy") { PrivacyScreen(navController) }
    }
}
