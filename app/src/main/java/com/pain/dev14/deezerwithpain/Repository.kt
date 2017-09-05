package com.pain.dev14.deezerwithpain

import com.github.simonpercic.oklog3.OkLogInterceptor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient

/**
 * Created by dev14 on 15.08.17.
 */
class Repository{
    private val instance: ApiService by lazy {
        val okLogInterceptor = OkLogInterceptor.builder().build()

        val okHttpBuilder = OkHttpClient.Builder()
        okHttpBuilder.addInterceptor(okLogInterceptor)
        val okHttpClient = okHttpBuilder.build()
        ApiService.create(okHttpClient)
    }

    fun getAlbum() = instance.getAlbum("/album/302127")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

}
