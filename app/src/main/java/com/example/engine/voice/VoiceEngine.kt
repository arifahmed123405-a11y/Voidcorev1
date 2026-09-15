package com.example.engine.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.core.state.AssistantState
import com.example.core.state.AssistantStateMachine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class VoiceEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _voiceParams = MutableStateFlow(VoiceParameters())
    val voiceParams: StateFlow<VoiceParameters> = _voiceParams.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<String>>(emptyList())
    val availableVoices: StateFlow<List<String>> = _availableVoices.asStateFlow()

    private var onSpeechDoneCallback: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            isInitialized = true
            loadAvailableVoices()
            applyCurrentParameters()

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    AssistantStateMachine.getInstance().transitionTo(AssistantState.SPEAKING)
                    startSpeechEnergySimulation()
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    AssistantStateMachine.getInstance().setAudioEnergy(0f)
                    AssistantStateMachine.getInstance().transitionTo(AssistantState.SLEEPING)
                    onSpeechDoneCallback?.invoke()
                    onSpeechDoneCallback = null
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    AssistantStateMachine.getInstance().setAudioEnergy(0f)
                    AssistantStateMachine.getInstance().transitionTo(AssistantState.SLEEPING)
                    onSpeechDoneCallback = null
                }
            })
        }
    }

    private fun loadAvailableVoices() {
        try {
            val voices = tts?.voices?.map { it.name }?.filter { it.contains("en", ignoreCase = true) } ?: emptyList()
            if (voices.isNotEmpty()) {
                _availableVoices.value = voices.take(8)
            } else {
                _availableVoices.value = listOf("en-us-x-sfg#female", "en-us-x-tpd#male", "en-gb-x-rjs#female", "en-us-default")
            }
        } catch (e: Exception) {
            _availableVoices.value = listOf("Default System Voice", "English (US)", "English (UK)")
        }
    }

    fun setPreset(preset: VoicePreset) {
        val current = _voiceParams.value
        _voiceParams.value = current.copy(
            preset = preset,
            pitch = preset.defaultPitch,
            speed = preset.defaultSpeed,
            warmth = preset.warmth,
            metallicResonance = preset.metallicResonance,
            syntheticDepth = preset.syntheticDepth,
            bass = preset.bass,
            glitchAmount = preset.glitchAmount
        )
        applyCurrentParameters()
    }

    fun updateParameters(params: VoiceParameters) {
        _voiceParams.value = params
        applyCurrentParameters()
    }

    fun setPitch(pitch: Float) {
        _voiceParams.value = _voiceParams.value.copy(pitch = pitch)
        tts?.setPitch(pitch)
    }

    fun setSpeed(speed: Float) {
        _voiceParams.value = _voiceParams.value.copy(speed = speed)
        tts?.setSpeechRate(speed)
    }

    fun selectVoiceByName(voiceName: String) {
        try {
            val matched = tts?.voices?.firstOrNull { it.name == voiceName }
            if (matched != null) {
                tts?.voice = matched
            }
        } catch (e: Exception) {
            // Fallback
        }
    }

    private fun applyCurrentParameters() {
        val params = _voiceParams.value
        tts?.setPitch(params.pitch)
        tts?.setSpeechRate(params.speed)
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!isInitialized) return
        cancelSpeech()
        onSpeechDoneCallback = onDone

        AssistantStateMachine.getInstance().updateSpeechOutput(text)
        val utteranceId = "void_utterance_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    /**
     * Interruption logic: stops TTS speech immediately and triggers microphone listening.
     */
    fun interruptAndListen(speechRecognizer: SpeechRecognitionManager) {
        tts?.stop()
        _isSpeaking.value = false
        AssistantStateMachine.getInstance().setAudioEnergy(0f)
        AssistantStateMachine.getInstance().transitionTo(AssistantState.LISTENING)
        speechRecognizer.startListening()
    }

    fun cancelSpeech() {
        if (_isSpeaking.value) {
            tts?.stop()
            _isSpeaking.value = false
            AssistantStateMachine.getInstance().setAudioEnergy(0f)
            AssistantStateMachine.getInstance().transitionTo(AssistantState.SLEEPING)
        }
    }

    private fun startSpeechEnergySimulation() {
        scope.launch {
            while (_isSpeaking.value) {
                // Simulate rhythmic speaking energy envelope
                val energy = (0.35f + (Math.random().toFloat() * 0.65f))
                AssistantStateMachine.getInstance().setAudioEnergy(energy)
                delay(80)
            }
            AssistantStateMachine.getInstance().setAudioEnergy(0f)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    companion object {
        @Volatile
        private var INSTANCE: VoiceEngine? = null

        fun getInstance(context: Context): VoiceEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VoiceEngine(context).also { INSTANCE = it }
            }
        }
    }
}
