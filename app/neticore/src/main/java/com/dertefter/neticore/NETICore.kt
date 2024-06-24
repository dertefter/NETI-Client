package com.dertefter.neticore

import android.app.Application
import android.util.Log
import com.dertefter.neticore.NETICoreClient.NETICoreClient
import com.dertefter.neticore.NETICoreClient.PersonHelper
import com.dertefter.neticore.NETICoreClient.dispace.DiSpaceClient
import com.dertefter.neticore.data.Event
import com.dertefter.neticore.data.NETICoreMetaData
import com.dertefter.neticore.data.schedule.Group
import com.dertefter.neticore.data.schedule.Week
import com.dertefter.neticore.local.AppPreferences

class NETICore {

    var appPreferences: AppPreferences? = null
    var metaData: NETICoreMetaData = NETICoreMetaData()
    var client: NETICoreClient? = null
    var diSpaceClient: DiSpaceClient? = null
    var personHelper: PersonHelper? = null

    fun initialize(application: Application){

        appPreferences = AppPreferences
        appPreferences?.setup(application)
        client = NETICoreClient(application, appPreferences!!)
        client?.initialize()
        diSpaceClient = DiSpaceClient(application, appPreferences!!)
        diSpaceClient?.initialize()
        personHelper = PersonHelper(application)

        checkInitialization()
    }

    private fun checkInitialization(): Boolean {
        return if (appPreferences != null && client != null) {
            metaData.initialized = true
            true
        }else{
            metaData.initialized = false
            false
        }
    }

    fun getInfo(): NETICoreMetaData {
        checkInitialization()
        return metaData
    }

    fun checkAuthorization(){
        if (appPreferences?.login != null && appPreferences?.password != null){
            login(appPreferences?.login!!, appPreferences?.password!!)
        }else{
            client?.authorizationStateViewModel?.setUnauthorized()
        }
    }

    fun login(login: String, password: String){
        client?.login(login, password)
    }

    fun getUserInfo(){
        client?.getUserInfo()
    }

    fun setGroup(group: Group?){
        client?.scheduleViewModel?.setGroup(group)

    }

    fun checkGroup() {
        Log.e("проверка группы", client?.scheduleViewModel?.currentGroupLiveData?.value.toString())
        if (client?.scheduleViewModel?.currentGroupLiveData?.value == null){
            if (appPreferences?.group == null){
                Log.e("checkGroup", "checkGroup: null")
                setGroup(null)
            } else if (appPreferences?.group == "individual"){
                Log.e("checkGroup", "checkGroup: individual")
                if (appPreferences?.gr_name != null){
                    val group = Group(title = appPreferences?.gr_name!!, isIndividual = true)
                    Log.e("checkGroup", "checkGroup: ${group}")
                    setGroup(group)
                }

                client?.getUserInfo()
            } else{
                val group = Group(title = appPreferences?.group!!, isIndividual = false)
                Log.e("checkGroup", "checkGroup: ${group}")
                setGroup(group)
            }
        }

    }

    fun getScheduleWeek(week: Week?){
        if (week != null){
            client?.scheduleViewModel?.loadScheduleWeek(week)
        }else{
            Log.e("getScheduleWeek", "week is null")
        }

    }

    fun logOut() {
        appPreferences?.login = null
        appPreferences?.password = null
        appPreferences?.token = null
        appPreferences?.group = null
        client?.authorizationStateViewModel?.setUnauthorized()
    }


}