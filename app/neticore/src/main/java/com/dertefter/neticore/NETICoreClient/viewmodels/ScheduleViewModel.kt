package com.dertefter.neticore.NETICoreClient.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dertefter.neticore.NETICoreClient.ResponseParser
import com.dertefter.neticore.TinyDB
import com.dertefter.neticore.api.APIService
import com.dertefter.neticore.data.Event
import com.dertefter.neticore.data.schedule.Group
import com.dertefter.neticore.data.schedule.Lesson
import com.dertefter.neticore.data.schedule.Schedule
import com.dertefter.neticore.data.schedule.SessiaScheduleItem
import com.dertefter.neticore.data.schedule.Week
import com.dertefter.neticore.local.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
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
    val sessiaScheduleLiveData = MutableLiveData<Event<List<SessiaScheduleItem>>>()
    val scheduleLiveDataList = mutableMapOf<String, MutableLiveData<Event<Schedule?>>>()
    val savedGroupsLiveData = MutableLiveData<List<Group>>()
    val groupListLiveData = MutableLiveData<Event<List<Group>>>()
    var currentGroupLiveData = MutableLiveData<Group>()
    var tinyDB : TinyDB = TinyDB(application.applicationContext)

    fun getLiveDataForWeek(weekQuery: String): MutableLiveData<Event<Schedule?>> {
        if (scheduleLiveDataList.containsKey(weekQuery)){
            return scheduleLiveDataList[weekQuery]!!
        } else {
            scheduleLiveDataList[weekQuery] = MutableLiveData<Event<Schedule?>>()
            return scheduleLiveDataList[weekQuery]!!
        }
    }


    fun saveGroup(group: Group){
        CoroutineScope(Dispatchers.IO).launch {
            var saved = tinyDB.getListObject("savedGroups", Group::class.java)
            if (saved == null){
                saved = ArrayList()
            }
            val index = saved.find { (it as Group).title == group.title }
            if (index != null){
                saved.remove(index)
            }
            saved.add(group)
            tinyDB.putListObject("savedGroups", saved as java.util.ArrayList<Any>)
            getSavedGroups()
        }

    }

    fun deleteGroup(group: Group){
        CoroutineScope(Dispatchers.IO).launch {
            val saved = tinyDB.getListObject("savedGroups", Group::class.java) ?: return@launch
            val name = group.title
            val index = saved.find { (it as Group).title == name }
            if (index != null){
                saved.remove(index)
            }
            tinyDB.putListObject("savedGroups", saved as java.util.ArrayList<Any>)
            getSavedGroups()
        }

    }

    fun getSavedGroups() {
        CoroutineScope(Dispatchers.IO).launch {
            val saved = tinyDB.getListObject("savedGroups", Group::class.java)?: return@launch
            savedGroupsLiveData.postValue(saved as List<Group>)
        }
    }


    fun loadWeekList(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (currentGroupLiveData.value != null){
                    weeksLiveData.postValue(Event.loading())
                    val url = "https://nstu.ru/studies/schedule/schedule_classes/"
                    val retrofit = Retrofit.Builder().baseUrl(url).client(okHttpClient).build()
                    val service = retrofit.create(APIService::class.java)
                    val response = service.getWeekList(group = currentGroupLiveData.value?.title)
                    if (response.isSuccessful){
                        val weeks = ResponseParser().parseWeeks(response.body(),  currentGroupLiveData.value!!)
                        tinyDB.putListObject("weeks", weeks as java.util.ArrayList<Any>)
                        weeksLiveData.postValue(Event.success(weeks))
                        if (scheduleLiveDataList.isNotEmpty()){
                            for (i in scheduleLiveDataList.keys){
                                getLiveDataForWeek(i).postValue(Event.loading())
                            }
                        }

                    }
                }
            } catch (e: Exception) {
                weeksLiveData.postValue(Event.error())
            } catch (e: Error) {
                weeksLiveData.postValue(Event.error())
            } catch (e: Throwable) {
                weeksLiveData.postValue(Event.error())
            } catch (e: JSONException) {
                weeksLiveData.postValue(Event.error())
            }
        }
    }

    fun loadSessiaSchedule(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                sessiaScheduleLiveData.postValue(Event.loading())
                val url = "https://www.nstu.ru/"
                val retrofit = Retrofit.Builder().baseUrl(url).client(okHttpClient).build()
                val service = retrofit.create(APIService::class.java)
                val response = service.getSessiaSchedule(group = currentGroupLiveData!!.value?.title)
                if (response.isSuccessful){
                    val sessiaSchedule = ResponseParser().parseSessiaSchedule(response.body())
                    Log.e("sesese", sessiaSchedule.toString())
                    sessiaScheduleLiveData.postValue(Event.success(sessiaSchedule))
                }

            } catch (e: Exception) {
                sessiaScheduleLiveData.postValue(Event.error())
            } catch (e: Error) {
                sessiaScheduleLiveData.postValue(Event.error())
            } catch (e: Throwable) {
                sessiaScheduleLiveData.postValue(Event.error())
            } catch (e: JSONException) {
                sessiaScheduleLiveData.postValue(Event.error())
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
        if (group != null){
            saveGroup(group)
        }
        currentGroupLiveData.postValue(group)
        if (group != null){
            if (group.isIndividual){
                appPreferences.group = "individual"
                appPreferences.gr_name = group.title
            }else{
                appPreferences.group = group.title
                appPreferences.gr_name = group.title
            }
        }

    }

    fun loadScheduleWeek(week: Week){
        Log.e("sasasasa days", getLiveDataForWeek(week.weekQuery).value?.data?.days.toString() )
        Log.e("sasasasa currentGroupLiveData", currentGroupLiveData.value?.title.toString())
        Log.e("sasasasa group", week.group?.title.toString())

        if (getLiveDataForWeek(week.weekQuery).value?.data?.days?.isEmpty() == false){
            if (currentGroupLiveData.value?.title == week.group?.title){
                return
            }
        }
        if (week.group?.isIndividual == true){
            loadIndividualSchedule(week)
        }else{
            loadGroupSchedule(week)
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
                        val schedule_with_rules = applyRules(schedule, weekQuery)
                        getLiveDataForWeek(week.weekQuery).postValue(Event.success(schedule_with_rules))
                    }
                }else{
                    val bodyString = tinyDB.getString("individual_schedule")
                    val schedule = ResponseParser().parseIndividualTimetable(bodyString, week.weekQuery)
                    Log.e("load from here", schedule.toString())
                    if (schedule != null){
                        val schedule_with_rules = applyRules(schedule, weekQuery)
                        getLiveDataForWeek(week.weekQuery).postValue(Event.success(schedule_with_rules))
                    }
                    else{
                        getLiveDataForWeek(week.weekQuery).postValue(Event.error())
                    }
                }

            } catch (e: Exception) {
                val bodyString = tinyDB.getString("individual_schedule")
                val schedule = ResponseParser().parseIndividualTimetable(bodyString, week.weekQuery)
                if (schedule != null){
                    val schedule_with_rules = applyRules(schedule, weekQuery)
                    getLiveDataForWeek(week.weekQuery).postValue(Event.success(schedule_with_rules))
                }
                else{
                    getLiveDataForWeek(week.weekQuery).postValue(Event.error())
                }
            } catch (e: Throwable) {
                val bodyString = tinyDB.getString("individual_schedule")
                val schedule = ResponseParser().parseIndividualTimetable(bodyString, week.weekQuery)
                if (schedule != null){
                    val schedule_with_rules = applyRules(schedule, weekQuery)
                    getLiveDataForWeek(week.weekQuery).postValue(Event.success(schedule_with_rules))
                }
                else{
                    getLiveDataForWeek(week.weekQuery).postValue(Event.error())
                }
            } catch (e: JSONException) {
                val bodyString = tinyDB.getString("individual_schedule")
                val schedule = ResponseParser().parseIndividualTimetable(bodyString, week.weekQuery)
                if (schedule != null){
                    val schedule_with_rules = applyRules(schedule, weekQuery)
                    getLiveDataForWeek(week.weekQuery).postValue(Event.success(schedule_with_rules))
                }
                else{
                    getLiveDataForWeek(week.weekQuery).postValue(Event.error())
                }
            }
        }

        //todo: FIX THIS

        /*
        todo номер два: я забыл, что тут было не так
         и что именно я должен был пофиксить.. пиздец
         */

    }

    fun getRules(weekQuery: Int, day: Int): java.util.ArrayList<Lesson>? {
        var saved = tinyDB.getListObject("scheduleRules$weekQuery$day", Lesson::class.java)
        if (saved == null){
            return null
        } else{
            return saved as java.util.ArrayList<Lesson>
        }
    }
    fun createRule(lesson: Lesson, customId: String){
        var saved = tinyDB.getListObject(customId, Lesson::class.java)
        if (saved == null){
            saved = ArrayList()
        }
        saved.add(lesson)
        tinyDB.putListObject(customId, saved)
    }

    fun deleteRule(lesson: Lesson, customId: String) {
        var saved = tinyDB.getListObject(customId, Lesson::class.java)
        if (saved == null){
            return
        }
        saved.remove(lesson)
        tinyDB.putListObject(customId, saved)
    }

    fun applyRules(schedule: Schedule, weekQuery: Int): Schedule {
        val modifiedDays = schedule.days!!.toMutableList()
        for (dayIndex in 0..5){
            if (!getRules(weekQuery, dayIndex).isNullOrEmpty()){
                val lessons = modifiedDays[dayIndex].lessons?.toMutableList()
                getRules(weekQuery, dayIndex)?.let { lessons?.addAll(it) }
                modifiedDays[dayIndex].lessons = lessons
            }
        }
        return schedule.copy(days = modifiedDays)
    }

    private fun loadGroupSchedule(week: Week) {
        CoroutineScope(Dispatchers.IO).launch {
            val weekQuery = week.weekQuery.toInt()
            try {
                getLiveDataForWeek(week.weekQuery).postValue(Event.loading())
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://nstu.ru/")
                    .build()
                val service = retrofit.create(APIService::class.java)
                val response = service.getScheduleGuest(group = week.group?.title, week = week.weekQuery)
                if (response.isSuccessful){
                    val bodyString = response.body()?.string()
                    tinyDB.putString("${week.group?.title}_${week.weekQuery}", bodyString)
                    val schedule = ResponseParser().parseGroupTimetable(bodyString)
                    if (schedule != null){
                        val schedule_with_rules = applyRules(schedule, weekQuery)
                        getLiveDataForWeek(week.weekQuery).postValue(Event.success(schedule_with_rules))
                    }else{
                        getLiveDataForWeek(week.weekQuery).postValue(Event.error())
                    }
                }

            }catch (e: Exception) {
                val bodyString = tinyDB.getString("${week.group?.title}_${week.weekQuery}")
                val schedule = ResponseParser().parseIndividualTimetable(bodyString, week.weekQuery)
                if (schedule != null){
                    val schedule_with_rules = applyRules(schedule, weekQuery)
                    getLiveDataForWeek(week.weekQuery).postValue(Event.success(schedule_with_rules))
                }
                else{
                    getLiveDataForWeek(week.weekQuery).postValue(Event.error())
                }
            } catch (e: Error) {
                val bodyString = tinyDB.getString("${week.group?.title}_${week.weekQuery}")
                val schedule = ResponseParser().parseIndividualTimetable(bodyString, week.weekQuery)
                if (schedule != null){
                    val schedule_with_rules = applyRules(schedule, weekQuery)
                    getLiveDataForWeek(week.weekQuery).postValue(Event.success(schedule_with_rules))
                }
                else{
                    getLiveDataForWeek(week.weekQuery).postValue(Event.error())
                }
            } catch (e: Throwable) {
                val bodyString = tinyDB.getString("${week.group?.title}_${week.weekQuery}")
                val schedule = ResponseParser().parseIndividualTimetable(bodyString, week.weekQuery)
                if (schedule != null){
                    val schedule_with_rules = applyRules(schedule, weekQuery)
                    getLiveDataForWeek(week.weekQuery).postValue(Event.success(schedule_with_rules))
                }
                else{
                    getLiveDataForWeek(week.weekQuery).postValue(Event.error())
                }
            } catch (e: JSONException) {
                val bodyString = tinyDB.getString("${week.group?.title}_${week.weekQuery}")
                val schedule = ResponseParser().parseIndividualTimetable(bodyString, week.weekQuery)
                if (schedule != null){
                    val schedule_with_rules = applyRules(schedule, weekQuery)
                    getLiveDataForWeek(week.weekQuery).postValue(Event.success(schedule_with_rules))
                }
                else{
                    getLiveDataForWeek(week.weekQuery).postValue(Event.error())
                }
            }
        }
    }

}