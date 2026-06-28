package com.mindmate.ai.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mindmate.ai.R
import com.mindmate.ai.data.model.Message
import com.mindmate.ai.service.VoiceService
import com.mindmate.ai.viewmodel.AuthViewModel
import com.mindmate.ai.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, viewModel: ChatViewModel, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    var textState by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val voiceService = remember {
        VoiceService(
            context = context,
            onResult = { result ->
                viewModel.isListening = false
                textState = result
            },
            onError = { error ->
                viewModel.isListening = false
                viewModel.error = error
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.isListening = true
            if (viewModel.useBackendVoice) {
                voiceService.startRecording()
            } else {
                val lang = authViewModel.user?.preferredLanguage ?: Locale.getDefault().language
                voiceService.startListening(lang)
            }
        } else {
            viewModel.error = "Microphone permission is required for voice input"
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceService.shutdown()
        }
    }

    LaunchedEffect(viewModel.messages.size) {
        if (viewModel.messages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.conversationId == null) stringResource(R.string.new_chat_title) else stringResource(R.string.chat_history_title)) },
                actions = {
                    IconButton(onClick = { viewModel.startNewChat() }) {
                        Icon(Icons.Default.Add, contentDescription = "New Chat")
                    }
                    IconButton(onClick = { navController.navigate("conversations") }) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(viewModel.messages) { message ->
                    ChatBubble(message)
                }
                if (viewModel.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.CenterStart) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
                if (viewModel.error != null) {
                    item {
                        Text(
                            text = viewModel.error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            if (viewModel.isListening) {
                WaveformIndicator()
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (viewModel.isListening) {
                            viewModel.isListening = false
                            if (viewModel.useBackendVoice) {
                                val file = voiceService.stopRecording()
                                if (file != null) {
                                    viewModel.transcribeAudio(file) { result ->
                                        textState = result
                                    }
                                }
                            } else {
                                voiceService.stopListening()
                            }
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                ) {
                    Icon(
                        if (viewModel.isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (viewModel.isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.type_message_hint)) },
                    maxLines = 4,
                    enabled = !viewModel.isLoading
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (textState.isNotBlank()) {
                            viewModel.sendMessage(
                                content = textState,
                                onResponse = { responseText ->
                                    val lang = authViewModel.user?.preferredLanguage ?: Locale.getDefault().language
                                    voiceService.speak(responseText, lang)
                                },
                                onStreamReady = { body ->
                                    voiceService.playAudioStream(body)
                                }
                            )
                            textState = ""
                        }
                    },
                    enabled = !viewModel.isLoading && textState.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
fun WaveformIndicator() {
    val infiniteTransition = rememberInfiniteTransition()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(5) { index ->
                val delay = index * 150
                val height by infiniteTransition.animateFloat(
                    initialValue = 10f,
                    targetValue = 30f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400, delayMillis = delay, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(height.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Listening...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ChatBubble(message: Message) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                color = color,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 0.dp,
                    bottomEnd = if (isUser) 0.dp else 16.dp
                )
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(12.dp),
                    color = textColor
                )
            }
            if (!isUser && message.mood != null) {
                Text(
                    text = "Mood: ${message.mood}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }
        }
    }
}
