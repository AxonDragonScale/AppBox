package com.axondragonscale.topty.core

import android.content.Context
import android.speech.tts.TextToSpeech
import com.axondragonscale.commons.logD
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Created by Ronak Harkhani on 04/11/24
 */
class TTS(private val context: Context) {

    companion object {
        private const val TAG = "TTS"
    }

    private var isInitialized = false
    private var tts: TextToSpeech? = null
        get() = field ?: TextToSpeech(context) { status ->
            TAG.logD { "Initialized TTS" }
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                isInitialized = true
            }
        }.also { field = it }


    suspend fun speak(code: String) {
        TAG.logD { "Speak start" }
        val tts = tts!! // Get tts (This makes sure the init starts)

        var timeout = 1000L
        while (!isInitialized && timeout >= 0) {
            delay(100)
            timeout -= 100
        }

        TAG.logD { "Speaking: $code" }
        val text = "The code is " + code.toList().joinToString(" ")
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun release() {
        if (isInitialized) {
            TAG.logD { "Releasing TTS" }
            tts?.stop()
            tts?.shutdown()
            tts = null
        }
    }
}
