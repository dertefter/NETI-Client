package com.dertefter.neticore.NETICoreClient

import android.app.Application
import com.dertefter.neticore.local.AppPreferences
import com.dertefter.neticore.NETICoreClient.viewmodels.AuthorizationStateViewModel
import com.dertefter.neticore.NETICoreClient.viewmodels.CampusPassViewModel
import com.dertefter.neticore.NETICoreClient.viewmodels.MessagesViewModel
import com.dertefter.neticore.NETICoreClient.viewmodels.MoneyViewModel
import com.dertefter.neticore.NETICoreClient.viewmodels.NewsViewModel
import com.dertefter.neticore.NETICoreClient.viewmodels.SchedulePersonViewModel
import com.dertefter.neticore.NETICoreClient.viewmodels.UserInfoViewModel
import com.dertefter.neticore.NETICoreClient.viewmodels.ScheduleViewModel
import com.dertefter.neticore.NETICoreClient.viewmodels.SearchPersonViewModel
import com.dertefter.neticore.NETICoreClient.viewmodels.SessiaResultsViewModel
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request

class NETICoreClient(
    val application: Application,
    val appPreferences: AppPreferences) {

    var okHttpClient: OkHttpClient? = null

    var authorizationStateViewModel: AuthorizationStateViewModel? = null
    var userInfoViewModel: UserInfoViewModel? = null
    var scheduleViewModel: ScheduleViewModel? = null
    var schedulePersonModel: SchedulePersonViewModel? = null
    var searchPersonViewModel: SearchPersonViewModel? = null
    var newsViewModel: NewsViewModel? = null
    var messagesViewModel: MessagesViewModel? = null
    var sessiaResultsViewModel: SessiaResultsViewModel? = null
    var campusPassViewModel: CampusPassViewModel? = null
    var moneyViewModel: MoneyViewModel? = null

    var isAuthorized: Boolean = false

    fun initialize(forAuth: Boolean = false){
        createOkHttpClient()
        if (authorizationStateViewModel == null){
            authorizationStateViewModel = AuthorizationStateViewModel(application, appPreferences, okHttpClient!!)
        }
        else{
            authorizationStateViewModel?.okHttpClient = okHttpClient!!
        }

        if (userInfoViewModel == null){
            userInfoViewModel = UserInfoViewModel(application, appPreferences, okHttpClient!!)
        }
        else{
            userInfoViewModel?.okHttpClient = okHttpClient!!
        }


        if (scheduleViewModel == null){
            scheduleViewModel = ScheduleViewModel(application, appPreferences, okHttpClient!!)
        }
        else{
            scheduleViewModel?.okHttpClient = okHttpClient!!
        }

        if (searchPersonViewModel == null){
            searchPersonViewModel = SearchPersonViewModel(application, appPreferences, okHttpClient!!)
        }else{
            searchPersonViewModel?.okHttpClient = okHttpClient!!
        }
        if (newsViewModel == null){
            newsViewModel = NewsViewModel(application, appPreferences, okHttpClient!!)
        }else{
            newsViewModel?.okHttpClient = okHttpClient!!
        }
        messagesViewModel = MessagesViewModel(application, appPreferences, okHttpClient!!)

        if (schedulePersonModel == null){
            schedulePersonModel = SchedulePersonViewModel(application, appPreferences, okHttpClient!!)
        }else{
            schedulePersonModel?.okHttpClient = okHttpClient!!
        }
        if (sessiaResultsViewModel == null){
            sessiaResultsViewModel = SessiaResultsViewModel(application, appPreferences, okHttpClient!!)
        }else{
            sessiaResultsViewModel?.okHttpClient = okHttpClient!!
        }
        if (campusPassViewModel == null){
            campusPassViewModel = CampusPassViewModel(application, appPreferences, okHttpClient!!)
        }else{
            campusPassViewModel?.okHttpClient = okHttpClient!!
        }
        if (moneyViewModel == null){
            moneyViewModel = MoneyViewModel(application, appPreferences, okHttpClient!!)
        }else{
            moneyViewModel?.okHttpClient = okHttpClient!!
        }

        if (forAuth){
            isAuthorized = true
        }

    }

    fun createOkHttpClient() {
        okHttpClient = OkHttpClient().newBuilder()
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val original: Request = chain.request()
                val authorized: Request = original.newBuilder()
                    .addHeader("Cookie", "NstuSsoToken=" + appPreferences.token)
                    .build()
                chain.proceed(authorized)
            }).build()
    }

    fun login(login: String, password: String) {
        authorizationStateViewModel?.tryAuthorize(login, password)
    }

    fun getUserInfo(){
        userInfoViewModel?.updateUserData()
    }

    fun updateWeeks(){
        scheduleViewModel?.loadWeekList()
    }


}