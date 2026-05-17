package com.apktados.ruleta.data.remote.rest

import com.apktados.ruleta.data.remote.GlobalPrizeRemote

data class GlobalPrizeRestDto(
    val amount: Int = 0,
    val updatedAt: Long = 0L,
    val lastWinnerUid: String? = null,
    val lastWinnerName: String? = null,
    val lastClaimedAmount: Int = 0
) {
    fun toRemote(): GlobalPrizeRemote {
        return GlobalPrizeRemote(
            amount = amount,
            updatedAt = updatedAt,
            lastWinnerUid = lastWinnerUid,
            lastWinnerName = lastWinnerName,
            lastClaimedAmount = lastClaimedAmount
        )
    }
}
