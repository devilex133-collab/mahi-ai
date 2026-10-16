package com.mahi.ai.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

/** Turn-based voice: one mic session or one utterance at a time. Call from the main thread. */
class VoiceController(private val ctx: Context) {
    enum class State { Idle, Listening, Speaking }

    private val _state = MutableStateFlow(State.Idle)
    val state: StateFlow<State> = _state

    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    init {
        tts = TextToSpeech(ctx) { ttsReady = it == TextToSpeech.SUCCESS }
    }

    fun listen(lang: String, onResult: (String) -> Unit, onError: (String) -> Unit) {
        if (_state.value != State.Idle) return
        if (!SpeechRecognizer.isRecognitionAvailable(ctx)) {
            onError("Speech recognition isn't available on this device."); return
        }
        recognizer?.destroy()
        val r = SpeechRecognizer.createSpeechRecognizer(ctx)
        recognizer = r
        r.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                _state.value = State.Idle
                val t = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (t.isNullOrBlank()) onError("I didn't catch that.") else onResult(t)
            }
            override fun onError(error: Int) {
                _state.value = State.Idle
                onError(
                    when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "I didn't hear anything. Try again."
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network error during speech recognition."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
                        else -> "Speech recognition error ($error)."
                    }
                )
            }
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang)
        }
        _state.value = State.Listening
        r.startListening(i)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    fun speak(text: String, lang: String, rate: Float) {
        val t = tts ?: return
        if (!ttsReady) return
        stop()
        t.language = Locale.forLanguageTag(lang)
        t.setSpeechRate(rate)
        t.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) { _state.value = State.Idle }
            override fun onError(utteranceId: String?) { _state.value = State.Idle }
        })
        _state.value = State.Speaking
        t.speak(text, TextToSpeech.QUEUE_FLUSH, null, "mahi")
    }

    fun stop() {
        recognizer?.destroy(); recognizer = null
        tts?.stop()
        _state.value = State.Idle
    }

    fun shutdown() { stop(); tts?.shutdown(); tts = null }
}
