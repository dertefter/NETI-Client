package com.dertefter.neticore.NETICoreClient.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dertefter.neticore.NETICoreClient.ResponseParser
import com.dertefter.neticore.TinyDB
import com.dertefter.neticore.api.APIService
import com.dertefter.neticore.data.Event
import com.dertefter.neticore.data.schedule.Group
import com.dertefter.neticore.data.schedule.Schedule
import com.dertefter.neticore.data.schedule.Week
import com.dertefter.neticore.local.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import retrofit2.Retrofit

class ScheduleViewModel(
    application: Application,
    val appPreferences: AppPreferences,
    var okHttpClient: OkHttpClient
) : AndroidViewModel(application) {

    val weeksLiveData = MutableLiveData<Event<List<Week>>>()

    val scheduleListLiveData = MutableLiveData<Event<List<Schedule?>?>>()
    var schList: MutableList<Schedule?>? = null

    val groupListLiveData = MutableLiveData<Event<List<Group>>>()

    var currentGroupLiveData = MutableLiveData<Group>()
    var tinyDB : TinyDB = TinyDB(application.applicationContext)
    fun loadWeekList(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                scheduleListLiveData.postValue(Event.loading())
                weeksLiveData.postValue(Event.loading())
                val url = "https://nstu.ru/studies/schedule/schedule_classes/"
                val retrofit = Retrofit.Builder().baseUrl(url).client(okHttpClient).build()
                val service = retrofit.create(APIService::class.java)
                val response = service.getWeekList(group = currentGroupLiveData!!.value?.title)
                if (response.isSuccessful){
                    val weeks = ResponseParser().parseWeeks(response.body(),  currentGroupLiveData?.value?.isIndividual!!)
                    val scheduleList = arrayOfNulls<Schedule>(weeks!!.size).toList()
                    tinyDB.putListObject("weeks", weeks as java.util.ArrayList<Any>)
                    schList = scheduleList.toMutableList()
                    scheduleListLiveData.postValue(Event.success(schList))
                    weeksLiveData.postValue(Event.success(weeks))
                }

            } catch (e: Exception) {
                setError()
            } catch (e: Error) {
                setError()
            } catch (e: Throwable) {
                setError()
            } catch (e: JSONException) {
                setError()
            }
        }
    }

    fun loadGroupList(searchQuery: String = ""){
        groupListLiveData.postValue(Event.loading())
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "https://nstu.ru/"
                val client = OkHttpClient().newBuilder()
                    .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                        val original: Request = chain.request()
                        val authorized: Request = original.newBuilder()
                            .addHeader("Cookie", "NstuSsoToken=" + AppPreferences.token)
                            .build()
                        chain.proceed(authorized)
                    })
                    .build()
                val retrofit = Retrofit.Builder().baseUrl(url).client(client).build()
                val service = retrofit.create(APIService::class.java)
                val response = service.getGroupList(searchQuery)
                if (response.isSuccessful){
                    val groupList = ResponseParser().parseGroups(response.body())
                    groupListLiveData.postValue(Event.success(groupList))
                }

            } catch (e: Exception) {
                groupListLiveData.postValue(Event.error())
            } catch (e: Error) {
                groupListLiveData.postValue(Event.error())
            } catch (e: Throwable) {
                groupListLiveData.postValue(Event.error())
            } catch (e: JSONException) {
                groupListLiveData.postValue(Event.error())
            }
        }
    }

    fun setGroup(group: Group?){
        currentGroupLiveData.postValue(group)
        weeksLiveData.value = (Event.loading())
        scheduleListLiveData.value = (Event.loading())
        if (group != null){
            if (group.isIndividual){
                appPreferences.group = "individual"
                appPreferences?.gr_name = group?.title
            }else{
                appPreferences.group = group.title
                appPreferences?.gr_name = group?.title
            }
        }

    }

    fun loadScheduleWeek(week: Week){
        if (week.isIndividual){
            loadIndividualSchedule(week)
        }else{
            loadGroupSchedule(week)
        }
    }

    fun setError(){
        val fromTiny = tinyDB.getListObject("weeks", Week::class.java)
        Log.e("tinyShit", "fromTiny: ${fromTiny.toString()}")
        if (fromTiny != null){
            schList = arrayOfNulls<Schedule>(fromTiny!!.size).toMutableList()
            scheduleListLiveData.postValue(Event.success(schList))
            weeksLiveData.postValue(Event.success(fromTiny as List<Week>))
        }else{
            weeksLiveData.postValue(Event.error())
        }

    }


    private fun loadIndividualSchedule(week: Week) {
        val weekQuery = week.weekQuery.toInt()
        val weekInArray = weekQuery - 1

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "https://ciu.nstu.ru/student_study/timetable/timetable_lessons/"
                val retrofit = Retrofit.Builder().baseUrl(url).client(okHttpClient).build()
                val service = retrofit.create(APIService::class.java)
                val response = service.basePage()
                if (response.isSuccessful){
                    val bodyString = response.body()?.string()
                    val schedule = ResponseParser().parseIndividualTimetable(bodyString, week.weekQuery)
                    if (schedule != null && !schedule.isError && !schedule.days.isNullOrEmpty()){
                        tinyDB.putString("individual_schedule", bodyString)
                        schList?.set(weekInArray, schedule)
                        scheduleListLiveData.postValue(Event.success(schList))
                    }
                    else{
                       throw Exception("Schedule is null")

                    }
                }else{
                    Log.e("momomo is not success", "idk")
                    val bodyString = tinyDB.getString("individual_schedule")
                    val schedule = ResponseParser().parseIndividualTimetable(bodyString, week.weekQuery)
                    Log.e("load from here", schedule.toString())
                    if (schedule != null){
                        schList?.set(weekInArray, schedule)
                        scheduleListLiveData.postValue(Event.success(schList))
                    }
                    else{
                        schList?.set(weekInArray, Schedule(isError = true))
                        scheduleListLiveData.postValue(Event.success(schList))
                    }
                }

            } catch (e: Exception) {
                val bodyString = tinyDB.getString("individual_schedule")
                val schedule = ResponseParser().parseIndividualTimetable(bodyString, week.weekQuery)
                Log.e("momomo schedule", schedule.toString())
                if (schedule != null){
                    schList?.set(weekInArray, schedule)
                    scheduleListLiveData.postValue(Event.success(schList))
                }
                else{
                    schList?.set(weekInArray, Schedule(isError = true))
                    scheduleListLiveData.postValue(Event.success(schList))
                }
            } catch (e: Throwable) {
                Log.e("momomo throwable", "idk")
                val bodyString = tinyDB.getString("individual_schedule")
                val schedule = ResponseParser().parseIndividualTimetable(bodyString, week.weekQuery)
                Log.e("load from here", schedule.toString())
                if (schedule != null){
                    schList?.set(weekInArray, schedule)
                    scheduleListLiveData.postValue(Event.success(schList))
                }
                else{
                    schList?.set(weekInArray, Schedule(isError = true))
                    scheduleListLiveData.postValue(Event.success(schList))
                }
            } catch (e: JSONException) {
                Log.e("momomo json", "idk")
                val bodyString = tinyDB.getString("individual_schedule")
                val schedule = ResponseParser().parseIndividualTimetable(bodyString, week.weekQuery)
                Log.e("load from here", schedule.toString())
                if (schedule != null){
                    schList?.set(weekInArray, schedule)
                    scheduleListLiveData.postValue(Event.success(schList))
                }
                else{
                    schList?.set(weekInArray, Schedule(isError = true))
                    scheduleListLiveData.postValue(Event.success(schList))
                }
            }
        }

        //todo: FIX THIS

        /*
        todo номер два: я забыл, что тут было не так
         и что именно я должен был пофиксить.. пиздец
         */

    }

    private fun loadGroupSchedule(week: Week) {
        val weekQuery = week.weekQuery.toInt()
        val weekInArray = weekQuery - 1
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://nstu.ru/")
                    .build()
                val service = retrofit.create(APIService::class.java)
                val response = service.getScheduleGuest(group = week.groupTitle, week = week.weekQuery)
                if (response.isSuccessful){
                    val bodyString = response.body()?.string()
                    tinyDB.putString("${week.groupTitle}_${week.weekQuery}", bodyString)
                    val schedule = ResponseParser().parseGroupTimetable(bodyString)
                    if (schedule != null){
                        schList?.set(weekInArray, schedule)
                        scheduleListLiveData.postValue(Event.success(schList))
                    }else{
                        schList?.set(weekInArray, Schedule(isError = true))
                        scheduleListLiveData.postValue(Event.success(schList))
                    }
                }

            }catch (e: Exception) {
                val bodyString = tinyDB.getString("${week.groupTitle}_${week.weekQuery}")
                val schedule = ResponseParser().parseIndividualTimetable(bodyString, week.weekQuery)
                if (schedule != null){
                    schList?.set(weekInArray, schedule)
                    scheduleListLiveData.postValue(Event.success(schList))
                }
                else{
                    schList?.set(weekInArray, Schedule(isError = true))
                    scheduleListLiveData.postValue(Event.success(schList))
                }
            } catch (e: Error) {
                val bodyString = tinyDB.getString("${week.groupTitle}_${week.weekQuery}")
                val schedule = ResponseParser().parseIndividualTimetable(bodyString, week.weekQuery)
                if (schedule != null){
                    schList?.set(weekInArray, schedule)
                    scheduleListLiveData.postValue(Event.success(schList))
                }
                else{
                    schList?.set(weekInArray, Schedule(isError = true))
                    scheduleListLiveData.postValue(Event.success(schList))
                }
            } catch (e: Throwable) {
                val bodyString = tinyDB.getString("${week.groupTitle}_${week.weekQuery}")
                val schedule = ResponseParser().parseIndividualTimetable(bodyString, week.weekQuery)
                if (schedule != null){
                    schList?.set(weekInArray, schedule)
                    scheduleListLiveData.postValue(Event.success(schList))
                }
                else{
                    schList?.set(weekInArray, Schedule(isError = true))
                    scheduleListLiveData.postValue(Event.success(schList))
                }
            } catch (e: JSONException) {
                val bodyString = tinyDB.getString("${week.groupTitle}_${week.weekQuery}")
                val schedule = ResponseParser().parseIndividualTimetable(bodyString, week.weekQuery)
                if (schedule != null){
                    schList?.set(weekInArray, schedule)
                    scheduleListLiveData.postValue(Event.success(schList))
                }
                else{
                    schList?.set(weekInArray, Schedule(isError = true))
                    scheduleListLiveData.postValue(Event.success(schList))
                }
            }
        }
    }

}