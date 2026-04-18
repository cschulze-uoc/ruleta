package com.apktados.ruleta.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MusicManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    var musicaActiva by mutableStateOf(true)
        private set

    fun startDefaultMusic() {
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

    fun isMusicActive(): Boolean {
        return musicaActiva
    }
    fun setCustomMusic(uri: Uri) {
        mediaPlayer?.reset()
        mediaPlayer?.setDataSource(context, uri)
        mediaPlayer?.prepare()
        mediaPlayer?.isLooping = true

        if (musicaActiva) {
            mediaPlayer?.start()
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}