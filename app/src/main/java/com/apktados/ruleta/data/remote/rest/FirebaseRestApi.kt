package com.apktados.ruleta.data.remote.rest

import retrofit2.http.GET

interface FirebaseRestApi {
    @GET("globalPrize.json")
    suspend fun getGlobalPrize(): GlobalPrizeRestDto?
}
