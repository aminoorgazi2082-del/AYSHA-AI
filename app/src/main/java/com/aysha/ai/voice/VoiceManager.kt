package com.aysha.ai.voice
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceManager(context: Context) : TextToSpeech.OnInitListener {
    private val app = context.applicationContext
    private var tts = TextToSpeech(app, this)
    private var ready = false
    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) tts.language = Locale.getDefault()
    }
    fun speak(text: String) { if (ready) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "aysha") }
    fun stop() { tts.stop() }
    fun listenIntent(): Intent? {
        if (!SpeechRecognizer.isRecognitionAvailable(app)) return null
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-IN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "AYSHA-কে বলুন")
        }
    }
}
