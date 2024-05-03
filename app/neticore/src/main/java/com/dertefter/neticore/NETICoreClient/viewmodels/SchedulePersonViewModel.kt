package com.dertefter.neticore.NETICoreClient.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dertefter.neticore.NETICoreClient.ResponseParser
import com.dertefter.neticore.api.APIService
import com.dertefter.neticore.data.Event
import com.dertefter.neticore.data.schedule.Schedule
import com.dertefter.neticore.data.schedule.Week
import com.dertefter.neticore.local.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONException
import retrofit2.Retrofit

class SchedulePersonViewModel(
    application: Application,
    val appPreferences: AppPreferences,
    var okHttpClient: OkHttpClient
) : AndroidViewModel(application) {

    val weeksLiveData = MutableLiveData<Event<List<Week>>>()

    val scheduleListLiveData = MutableLiveData<Event<List<Schedule?>?>>()

    fun loadWeekList(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val weeks = ResponseParser().get18Weeks()
                val scheduleList = arrayOfNulls<Schedule>(weeks.size).toList()
                scheduleListLiveData.postValue(Event.success(scheduleList))
                weeksLiveData.postValue(Event.success(weeks))
            } catch (e: Exception) {
                Log.e("zzzzzzzzzz", e.toString())
                setError()
            } catch (e: Error) {
                Log.e("zzzzz", e.toString())
                setError()
            } catch (e: Throwable) {
                Log.e("zzzzz", e.toString())
                setError()
            } catch (e: JSONException) {
                Log.e("zzzzz", e.toString())
                setError()
            }
        }
    }

    fun loadScheduleWeek(personId: String, week: Week){
        loadSchedule(personId, week)
    }

    fun setError(){
        weeksLiveData.postValue(Event.error())
    }


    private fun loadSchedule(personId: String, week: Week) {
        val weekQuery = week.weekQuery.toInt()
        val weekInArray = weekQuery - 1

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "https://ciu.nstu.ru/kaf/persons/${personId}/edu_actions/timetables/lessons/"
                val retrofit = Retrofit.Builder().baseUrl(url).client(okHttpClient).build()
                val service = retrofit.create(APIService::class.java)
                val response = service.basePage()
                if (response.isSuccessful){
                    val schedule = ResponseParser().parsePersonTimetable(response.body(), week.weekQuery)
                    val scheduleList = scheduleListLiveData.value?.data?.toMutableList()
                    if (schedule != null){
                        scheduleList?.set(weekInArray, schedule)
                        scheduleListLiveData.postValue(Event.success(scheduleList))
                    }else{
                        val scheduleList = scheduleListLiveData.value?.data?.toMutableList()
                        scheduleList?.set(weekInArray, Schedule())
                        scheduleListLiveData.postValue(Event.success(scheduleList))
                    }
                }

            } catch (e: Exception) {
                Log.e("zzzzzzzzzzzzzzzzzzz", e.stackTraceToString())

            } catch (e: Error) {
                Log.e("zzzzzzzzzzzzzzzzzzz", e.stackTraceToString())
                setError()
            } catch (e: Throwable) {
                Log.e("zzzzzzzzzzzzzzzzzzz", e.stackTraceToString())
                setError()
            } catch (e: JSONException) {
                Log.e("zzzzzzzzzzzzzzzzzzz", e.stackTraceToString())
                setError()
            }
        }

    }
}