package com.media.schoolday

/*
 * Created by yosi on 19/05/2017.
 */


import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.media.schoolday.api.ApiService
import com.media.schoolday.utility.JSONSharedPreferences
import com.onesignal.OneSignal
import com.orhanobut.wasp.Wasp
import com.orhanobut.wasp.utils.LogLevel
import okhttp3.Interceptor

class SchoolApp: Application(){
    companion object {
        var prefs: JSONSharedPreferences? = null
        var rest: ApiService? = null
        var user: FirebaseAuth? = null

    }

    override fun onCreate() {
        super.onCreate()
        prefs = JSONSharedPreferences(this)
        user = FirebaseAuth.getInstance()
        restApi()

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init()
    }

    private fun restApi(){
        //cloud server
        val url = "http://128.199.199.232/web/api"
        rest = Wasp.Builder(this)
                .setEndpoint(url)
                .setLogLevel(LogLevel.FULL_REST_ONLY)
                .build().create(ApiService::class.java)
    }

    fun getRequestInterceptor(): Interceptor {
        return Interceptor { chain ->
            chain.proceed(
                    chain.request().newBuilder()
                            .header("key","value")
                            .build()
            )}
    }

}