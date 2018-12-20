package com.solidscorpion.medic

import android.app.Application
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.firebase.messaging.FirebaseMessaging
import com.pushwoosh.Pushwoosh
import com.solidscorpion.medic.api.Api
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class MedicApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Pushwoosh.getInstance().registerForPushNotifications()
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
        }
    }

    val api: Api by lazy {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
        })
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, null)
        val sslSocketFactory = sslContext.socketFactory
        val builder = OkHttpClient.Builder()
        builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
        builder.hostnameVerifier { _, _ -> true }
        builder.addNetworkInterceptor(StethoInterceptor())
        val build = Retrofit.Builder()
            .baseUrl("https://dev.medic.co.il/")
            .client(builder.build())
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder()
                .add(KotlinJsonAdapterFactory()).build()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        build.create(Api::class.java)
    }

}