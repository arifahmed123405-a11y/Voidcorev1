package com.example.engine.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.core.state.AssistantState
import com.example.core.state.AssistantStateMachine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Robust Speech Recognition Manager.
 * Manages real microphone input using Android SpeechRecognizer APIs,
 * provides live partial and final transcription flows, handles audio energy levels,
 * and defends against recognizer-busy overlap, lifecycle leaks, and timeout errors.
 */
class SpeechRecognitionManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    var onFinalResult: ((String) -> Unit)? = null
    var onPartialResult: ((String) -> Unit)? = null

    var onSpeechResult: ((String) -> Unit)?
        get() = onFinalResult
        set(value) { onFinalResult = value }

    init {
        mainHandler.post { initRecognizer() }
    }

    private fun initRecognizer() {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _isListening.value = true
                            AssistantStateMachine.getInstance().transitionTo(AssistantState.LISTENING)
                        }

                        override fun onBeginningOfSpeech() {
                            _isListening.value = true
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                            AssistantStateMachine.getInstance().setAudioEnergy(normalized)
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _isListening.value = false
                            AssistantStateMachine.getInstance().setAudioEnergy(0f)
                        }

                        override fun onError(error: Int) {
                            _isListening.value = false
                            AssistantStateMachine.getInstance().setAudioEnergy(0f)
                            
                            when (error) {
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                                    // Protect against recognizer-busy overlap by canceling cleanly
                                    try {
                                        speechRecognizer?.cancel()
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                }
                                SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                                    // Soft timeout, assistant transitions gracefully
                                    if (AssistantStateMachine.getInstance().currentState.value == AssistantState.LISTENING) {
                                        AssistantStateMachine.getInstance().transitionTo(AssistantState.SLEEPING)
                                    }
                                }
                                else -> {
                                    // Non-fatal error recovery
                                }
                            }
                        }

                        override fun onResults(results: Bundle?) {
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val text = matches?.firstOrNull() ?: ""
                            _isListening.value = false
                            _liveTranscript.value = text
                            AssistantStateMachine.getInstance().updateTranscript(text)
                            if (text.isNotBlank()) {
                                onFinalResult?.invoke(text)
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val partial = matches?.firstOrNull() ?: ""
                            _liveTranscript.value = partial
                            AssistantStateMachine.getInstance().updateTranscript(partial)
                            onPartialResult?.invoke(partial)
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }
            }
        } catch (e: Exception) {
            // Graceful handling for non-GMS / headless environments
        }
    }

    fun startListening() {
        mainHandler.post {
            try {
                if (_isListening.value) {
                    cancelListening()
                }
                if (speechRecognizer == null) {
                    initRecognizer()
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }

                _liveTranscript.value = ""
                AssistantStateMachine.getInstance().updateTranscript("")
                AssistantStateMachine.getInstance().transitionTo(AssistantState.LISTENING)
                speechRecognizer?.startListening(intent)
                _isListening.value = true
            } catch (e: Exception) {
                _isListening.value = false
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                // Ignore
            }
            _isListening.value = false
            AssistantStateMachine.getInstance().setAudioEnergy(0f)
        }
    }

    fun cancelListening() {
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                // Ignore
            }
            _isListening.value = false
            AssistantStateMachine.getInstance().setAudioEnergy(0f)
        }
    }

    fun destroy() {
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                // Ignore
            }
            speechRecognizer = null
            _isListening.value = false
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SpeechRecognitionManager? = null

        fun getInstance(context: Context): SpeechRecognitionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SpeechRecognitionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

