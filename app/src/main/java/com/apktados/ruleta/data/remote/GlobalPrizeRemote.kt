package com.apktados.ruleta.data.remote

data class GlobalPrizeRemote(
    val amount: Int = 0,
    val updatedAt: Long = 0L,
    val lastWinnerUid: String? = null,
    val lastWinnerName: String? = null,
    val lastClaimedAmount: Int = 0
)
