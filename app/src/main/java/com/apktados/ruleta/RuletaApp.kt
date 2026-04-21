package com.apktados.ruleta

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.apktados.ruleta.audio.MusicManager
class RuletaApp : Application(), DefaultLifecycleObserver {
    lateinit var musicManager: MusicManager
    override fun onCreate() {
        super<Application>.onCreate()

        musicManager = MusicManager(this)
        musicManager.startDefaultMusic()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        musicManager.pauseMusic()
    }

    override fun onStart(owner: LifecycleOwner) {
        if (musicManager.musicaActiva) {
            musicManager.resumeMusic()
        }
    }
}