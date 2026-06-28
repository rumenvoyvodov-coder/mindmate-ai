package com.mindmate.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "MindMate AI Privacy Policy",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = """
                    Last Updated: June 20, 2026

                    Your privacy is important to us. This Privacy Policy explains how MindMate AI collects, uses, and protects your information.

                    1. Data Collection
                    - Email Address: Collected for account creation and authentication.
                    - Chat Messages: We store your conversations to provide history and personalized AI responses.
                    - Audio Recordings: If you use voice input, audio is processed to transcribe text. We do not store raw audio long-term unless specified.
                    
                    2. Use of Data
                    - To provide and improve the MindMate AI service.
                    - To personalize your experience.
                    - To process subscriptions and premium features.

                    3. Data Security
                    - We use industry-standard encryption to protect your data during transit and at rest.
                    - Your conversations are private and only accessible to you.

                    4. Third-Party Services
                    - We use OpenAI/Anthropic APIs for AI processing. Data sent to these providers is subject to their privacy policies.
                    - We use Google Firebase for cloud messaging and app analytics.

                    5. Your Rights
                    - You can delete your account and all associated data at any time through the app settings.
                    
                    Contact us at privacy@mindmate.ai if you have any questions.
                """.trimIndent(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
