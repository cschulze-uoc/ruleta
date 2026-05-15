package com.apktados.ruleta.data.remote.rest

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object FirebaseRestClient {
    private const val BASE_URL =
        "https://apktados-ruleta-default-rtdb.europe-west1.firebasedatabase.app/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val api: FirebaseRestApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(FirebaseRestApi::class.java)
}
