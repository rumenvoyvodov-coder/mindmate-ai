package com.mindmate.ai.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.work.*
import com.mindmate.ai.R
import com.mindmate.ai.service.ReminderWorker
import com.mindmate.ai.viewmodel.AuthViewModel
import com.mindmate.ai.viewmodel.ChatViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, authViewModel: AuthViewModel, chatViewModel: ChatViewModel) {
    val context = LocalContext.current
    var notificationsEnabled by remember { mutableStateOf(true) }
    var dailyRemindersEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.loadProfile()
        chatViewModel.loadUsage()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationsEnabled = isGranted
    }

    fun scheduleReminder(enabled: Boolean) {
        val workManager = WorkManager.getInstance(context)
        if (enabled) {
            val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(24, TimeUnit.HOURS)
                .addTag("daily_reminder")
                .build()
            workManager.enqueueUniquePeriodicWork(
                "daily_reminder",
                ExistingPeriodicWorkPolicy.UPDATE,
                reminderRequest
            )
        } else {
            workManager.cancelAllWorkByTag("daily_reminder")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
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
        ) {
            Text(text = stringResource(R.string.profile_section), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "${stringResource(R.string.name_label)}: ${authViewModel.user?.name ?: "..."}")
            Text(text = "Email: ${authViewModel.user?.email ?: "..."}")
            Text(text = "${stringResource(R.string.language_label)}: ${authViewModel.user?.preferredLanguage ?: "..."}")
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(text = stringResource(R.string.voice_section), style = MaterialTheme.typography.titleLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Read AI Responses (TTS)")
                Switch(
                    checked = chatViewModel.ttsEnabled,
                    onCheckedChange = { chatViewModel.ttsEnabled = it }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Use High-Quality Voice (Cloud)")
                Switch(
                    checked = chatViewModel.useBackendVoice,
                    onCheckedChange = { chatViewModel.useBackendVoice = it }
                )
            }
            if (chatViewModel.useBackendVoice) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Voice Persona")
                    Row {
                        FilterChip(
                            selected = chatViewModel.voiceType == "therapist",
                            onClick = { chatViewModel.voiceType = "therapist" },
                            label = { Text("Therapist") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = chatViewModel.voiceType == "coach",
                            onClick = { chatViewModel.voiceType = "coach" },
                            label = { Text("Coach") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(text = stringResource(R.string.notifications_section), style = MaterialTheme.typography.titleLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Push Notifications")
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            notificationsEnabled = enabled
                        }
                    }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Daily Reminders")
                Switch(
                    checked = dailyRemindersEnabled,
                    onCheckedChange = { 
                        dailyRemindersEnabled = it
                        scheduleReminder(it)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(text = stringResource(R.string.subscription_section), style = MaterialTheme.typography.titleLarge)
            Text(text = "Current Tier: ${authViewModel.user?.tier?.uppercase() ?: "..."}")
            if (authViewModel.user?.tier == "free") {
                Button(onClick = { navController.navigate("premium") }, modifier = Modifier.padding(top = 8.dp)) {
                    Text(stringResource(R.string.upgrade_button))
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))

            TextButton(
                onClick = { navController.navigate("privacy") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.privacy_policy))
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = { 
                    authViewModel.logout()
                    navController.navigate("login") { popUpTo("login") { inclusive = true } } 
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.logout_button))
            }
        }
    }
}
