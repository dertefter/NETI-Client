package com.dertefter.neticore.NETICoreClient.dispace

import android.app.Application
import com.dertefter.neticore.NETICoreClient.dispace.filemanager.FileManagerViewModel
import com.dertefter.neticore.NETICoreClient.dispace.viewmodels.DiAuthorizationStateViewModel
import com.dertefter.neticore.NETICoreClient.dispace.viewmodels.DiCourcesViewModel
import com.dertefter.neticore.NETICoreClient.dispace.viewmodels.DiMessagesViewModel
import com.dertefter.neticore.local.AppPreferences
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request

class DiSpaceClient(
    val application: Application,
    val appPreferences: AppPreferences) {

    var okHttpClient: OkHttpClient? = null
    var fileManagerViewModel: FileManagerViewModel? = null


    var authorizationStateViewModel: DiAuthorizationStateViewModel? = null
    var diMessagesViewModel: DiMessagesViewModel? = null
    var diCourcesViewModel: DiCourcesViewModel? = null

    var isAuthorized: Boolean = false

    fun initialize(){
        createOkHttpClient()
        if (fileManagerViewModel == null){
            fileManagerViewModel = FileManagerViewModel(application, appPreferences, okHttpClient!!)
        }else{
            fileManagerViewModel?.okHttpClient = okHttpClient!!
        }
        if (authorizationStateViewModel == null){
            authorizationStateViewModel = DiAuthorizationStateViewModel(application, appPreferences, okHttpClient!!)
        } else{
            authorizationStateViewModel?.okHttpClient = okHttpClient!!
        }
        if (diMessagesViewModel == null){
            diMessagesViewModel = DiMessagesViewModel(application, appPreferences, okHttpClient!!)
        } else{
            diMessagesViewModel?.okHttpClient = okHttpClient!!
        }
        if (diCourcesViewModel == null){
            diCourcesViewModel = DiCourcesViewModel(application, appPreferences, okHttpClient!!)
        } else{
            diCourcesViewModel?.okHttpClient = okHttpClient!!
        }
    }

    fun createOkHttpClient() {
        val cookieJar = object : CookieJar {
            public val cookieStore: HashMap<String, List<Cookie>> = HashMap()
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore[url.host] = cookies
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                val cookies = cookieStore[url.host]
                return cookies ?: ArrayList()
            }
        }
        okHttpClient = OkHttpClient().newBuilder().cookieJar(cookieJar)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + appPreferences.token)
                    .build()
                chain.proceed(authorized)
            }).build()
    }

    fun auth() {
        authorizationStateViewModel?.tryAuthorize()
    }


}