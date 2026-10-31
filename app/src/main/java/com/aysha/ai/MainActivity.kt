package com.aysha.ai

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.aysha.ai.ai.GeminiService
import com.aysha.ai.controller.AppController
import com.aysha.ai.memory.MemoryManager
import com.aysha.ai.settings.SecureStorage
import com.aysha.ai.settings.SettingsStore
import com.aysha.ai.voice.VoiceManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AyshaHome() }
    }
}

private data class Message(val text: String, val user: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AyshaHome() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ai = remember { GeminiService() }
    val secure = remember { SecureStorage(context) }
    val prefs = remember { SettingsStore(context) }
    val memory = remember { MemoryManager(context) }
    val voice = remember { VoiceManager(context) }
    val phone = remember { AppController(context) }

    var chat by remember { mutableStateOf(listOf(Message("হ্যালো! আমি AYSHA AI। কীভাবে সাহায্য করতে পারি?", false))) }
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("ONLINE • READY") }
    var settingsOpen by remember { mutableStateOf(false) }
    var memoryOpen by remember { mutableStateOf(false) }

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            input = result.data?.getStringArrayListExtra("android.speech.extra.RESULTS")?.firstOrNull().orEmpty()
        }
    }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) voice.listenIntent()?.let { speechLauncher.launch(it) }
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Color(0xFFBD91FF), secondary = Color(0xFF8855C7),
        background = Color(0xFF090711), surface = Color(0xFF1B1428)
    )) {
        Column(Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF291541), Color(0xFF090711), Color(0xFF07060B)))
        ).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("AYSHA AI", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    Text(status, color = Color(0xFFBD91FF), fontSize = 10.sp)
                }
                TextButton(onClick = { memoryOpen = true }) { Text("MEMORY", color = Color.White) }
                IconButton(onClick = { settingsOpen = true }) { Icon(Icons.Default.Settings, "Settings", tint = Color.White) }
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(Modifier.size(120.dp).background(
                    Brush.radialGradient(listOf(Color(0xFFBD91FF), Color(0xFF603B91), Color.Transparent)), CircleShape
                ), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(68.dp).background(Color(0xFF281642), CircleShape), contentAlignment = Alignment.Center) {
                        Text("A", color = Color.White, fontSize = 36.sp)
                    }
                }
            }
            Text("আপনার AI সহকারী", color = Color.White, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(12.dp))
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(chat) { msg ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (msg.user) Arrangement.End else Arrangement.Start) {
                        Surface(color = if (msg.user) Color(0xFF59318B) else Color(0xFF211A30),
                            shape = RoundedCornerShape(16.dp), modifier = Modifier.widthIn(max = 310.dp)) {
                            Text(msg.text, color = Color.White, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f),
                    placeholder = { Text("AYSHA-কে কিছু জিজ্ঞাসা করুন...") }, maxLines = 4
                )
                IconButton(onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        voice.listenIntent()?.let { speechLauncher.launch(it) }
                    } else micPermission.launch(Manifest.permission.RECORD_AUDIO)
                }) { Icon(Icons.Default.Mic, "Microphone", tint = Color.White) }
                IconButton(enabled = !busy, onClick = {
                    val question = input.trim()
                    if (question.isNotEmpty()) {
                        chat = chat + Message(question, true); input = ""; busy = true; status = "THINKING..."
                        scope.launch {
                            val answer = ai.generate(question, secure.read(), prefs.model)
                                .getOrElse { "সমস্যা হয়েছে: ${it.message ?: "Unknown error"}" }
                            chat = chat + Message(answer, false)
                            if (prefs.voice) voice.speak(answer)
                            busy = false; status = "ONLINE • READY"
                        }
                    }
                }) { Icon(Icons.Default.Send, "Send", tint = Color(0xFFBD91FF)) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { voice.stop() }) {
                    Icon(Icons.Default.Stop, null); Text("Stop voice")
                }
            }
        }

        if (settingsOpen) {
            var key by remember { mutableStateOf(secure.read()) }
            var model by remember { mutableStateOf(prefs.model) }
            var voiceOn by remember { mutableStateOf(prefs.voice) }
            AlertDialog(
                onDismissRequest = { settingsOpen = false },
                title = { Text("AYSHA Settings") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Gemini API Key")
                        OutlinedTextField(key, { key = it }, singleLine = true, placeholder = { Text("Paste API key") })
                        Text("Gemini model")
                        OutlinedTextField(model, { model = it }, singleLine = true)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Voice replies", Modifier.weight(1f)); Switch(voiceOn, { voiceOn = it })
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        secure.save(key.trim()); prefs.model = model.trim().ifBlank { "gemini-2.5-flash" }
                        prefs.voice = voiceOn; settingsOpen = false
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        scope.launch {
                            status = if (ai.generate("Reply only: API connection successful", key.trim(), model.trim()).isSuccess)
                                "API TEST SUCCESS" else "API TEST FAILED"
                        }
                    }) { Text("Test API") }
                }
            )
        }

        if (memoryOpen) {
            var value by remember { mutableStateOf("") }
            var version by remember { mutableIntStateOf(0) }
            AlertDialog(
                onDismissRequest = { memoryOpen = false },
                title = { Text("Saved memories") },
                text = {
                    Column {
                        OutlinedTextField(value, { value = it }, placeholder = { Text("Approved preference or project note") })
                        Spacer(Modifier.height(8.dp))
                        key(version) {
                            memory.list().forEach { item ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(item, Modifier.weight(1f), fontSize = 12.sp)
                                    TextButton(onClick = { memory.delete(item); version++ }) { Text("Delete") }
                                }
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { memory.add(value); value = ""; version++ }) { Text("Save") } },
                dismissButton = { TextButton(onClick = { memory.clear(); version++ }) { Text("Clear all") } }
            )
        }
    }
}
