package com.apktados.ruleta

import android.app.Application
import com.apktados.ruleta.audio.MusicManager
class RuletaApp : Application() {
    lateinit var musicManager: MusicManager
    override fun onCreate() {
        super.onCreate()

        musicManager = MusicManager(this)
        musicManager.startDefaultMusic()
    }
}