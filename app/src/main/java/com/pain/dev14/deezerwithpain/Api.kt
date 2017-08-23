package com.pain.dev14.deezerwithpain

import io.reactivex.Flowable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Created by dev14 on 15.08.17.
 */
interface ApiService {

    @GET
    fun getAlbum(@Url url: String): Flowable<Album>

    companion object Factory {
        fun create(okHttpClient: OkHttpClient): ApiService {
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("http://api.deezer.com")
                    .client(okHttpClient)
                    .build()

            return retrofit.create(ApiService::class.java);
        }
    }
}