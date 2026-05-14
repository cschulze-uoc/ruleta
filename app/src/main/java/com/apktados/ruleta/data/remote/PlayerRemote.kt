package com.apktados.ruleta.data.remote

data class PlayerRemote(
    val uid: String = "",
    val displayName: String? = null,
    val email: String? = null,
    val victories: Int = 0,
    val bestScore: Int = 0,
    val lastScore: Int = 0,
    val updatedAt: Long = 0L
)
