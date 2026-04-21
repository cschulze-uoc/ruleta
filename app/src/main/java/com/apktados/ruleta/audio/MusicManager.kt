package com.apktados.ruleta.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager

class MusicManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    var musicaActiva by mutableStateOf(true)
        private set

    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var audioFocusRequest: AudioFocusRequest? = null

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                pauseMusic()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pauseMusic()
            }

            AudioManager.AUDIOFOCUS_GAIN -> {
                if (musicaActiva) resumeMusic()
            }
        }
    }

    fun startDefaultMusic() {
        if (!requestAudioFocus()) return

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, com.apktados.ruleta.R.raw.musica)
            mediaPlayer?.isLooping = true
        }

        if (musicaActiva) {
            mediaPlayer?.start()
        }
    }

    fun toggleMusic() {
        musicaActiva = !musicaActiva
        if (musicaActiva) {
            mediaPlayer?.start()
        } else {
            mediaPlayer?.pause()
        }
    }

    fun pauseMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    fun resumeMusic() {
        if (musicaActiva) {
            mediaPlayer?.let {
                if (!it.isPlaying) {
                    it.start()
                }
            }
        }
    }

    fun setCustomMusic(uri: Uri) {
        if (!requestAudioFocus()) return

        mediaPlayer?.reset()
        mediaPlayer?.setDataSource(context, uri)
        mediaPlayer?.prepare()
        mediaPlayer?.isLooping = true

        if (musicaActiva) {
            mediaPlayer?.start()
        }
    }

    private fun requestAudioFocus(): Boolean {
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setOnAudioFocusChangeListener(focusChangeListener)
            .build()
        audioFocusRequest = request

        val result = audioManager.requestAudioFocus(request)
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    fun isMusicActive(): Boolean {
        return musicaActiva
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null

        audioFocusRequest?.let {
            audioManager.abandonAudioFocusRequest(it)
        }
    }
}