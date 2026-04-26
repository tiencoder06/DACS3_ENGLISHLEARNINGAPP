package com.example.englishlearningapp.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import com.example.englishlearningapp.data.model.Vocabulary
import java.util.Locale

class PronunciationHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
            }
        }
    }

    /**
     * Quy tắc phát âm nghiêm ngặt:
     * 1. Ưu tiên 1: audioUrl không rỗng -> Phát audioUrl
     * 2. Ưu tiên 2: audioText không rỗng -> Dùng TTS đọc audioText
     * 3. Ưu tiên 3: audioText rỗng -> Dùng TTS đọc word
     */
    fun playPronunciation(vocab: Vocabulary) {
        if (vocab.audioUrl.isNotEmpty()) {
            playFromUrl(vocab.audioUrl)
        } else {
            val textToRead = vocab.audioText.ifEmpty { vocab.word }
            speakTts(textToRead)
        }
    }

    private fun playFromUrl(url: String) {
        try {
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener { start() }
                setOnCompletionListener { release() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun speakTts(text: String) {
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
