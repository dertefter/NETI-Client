package com.dertefter.neticore.NETICoreClient.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dertefter.neticore.NETICoreClient.ResponseParser
import com.dertefter.neticore.api.APIService
import com.dertefter.neticore.data.Event
import com.dertefter.neticore.data.User
import com.dertefter.neticore.data.campus_pass.CampusPassData
import com.dertefter.neticore.data.campus_pass.CampusPassDate
import com.dertefter.neticore.data.campus_pass.CampusPassTime
import com.dertefter.neticore.data.sessia_results.SessiaResults
import com.dertefter.neticore.local.AppPreferences
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Retrofit


class CampusPassViewModel(
    application: Application,
    val appPreferences: AppPreferences,
    var okHttpClient: OkHttpClient
) : AndroidViewModel(application) {

    val campusPassLiveData = MutableLiveData<Event<CampusPassData>>()
    val campusPassTimesLiveData = MutableLiveData<Event<List<CampusPassTime>>>()
    val selectedDate = MutableLiveData<CampusPassDate?>()

    fun selectDate(date: CampusPassDate?){
        if (date == null){
            selectedDate.postValue(null)
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                selectedDate.postValue(date)
                val url1 = "https://ciu.nstu.ru/student_study/campus_pass/"
                val retrofit = Retrofit.Builder().baseUrl(url1).client(okHttpClient).build()
                val service = retrofit.create(APIService::class.java)
                val response = service.postForm(hashMapOf("selected_date" to date.data_calendar_date))
                if (response.isSuccessful) {
                    val parsedData = ResponseParser().parseCampusPassTimes(response.body())
                    campusPassTimesLiveData.postValue(Event.success(parsedData))
                }else{
                    campusPassTimesLiveData.postValue(Event.error())
                }

            } catch (e: Exception) {
                Log.e("campusPassTimesLiveData", ": ${e.stackTraceToString()}")
                campusPassTimesLiveData.postValue(Event.error())
            } catch (e: Error) {
                Log.e("campusPassTimesLiveData", ": ${e.stackTraceToString()}")
                campusPassTimesLiveData.postValue(Event.error())
            } catch (e: Throwable) {
                Log.e("campusPassTimesLiveData", ": ${e.stackTraceToString()}")
                campusPassTimesLiveData.postValue(Event.error())
            } catch (e: JSONException) {
                Log.e("campusPassTimesLiveData", ": ${e.stackTraceToString()}")
                campusPassTimesLiveData.postValue(Event.error())
            }
        }


    }

    fun updateCampusPassData(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                selectedDate.postValue(null)
                campusPassLiveData.postValue(Event.loading())

                val url1 = "https://ciu.nstu.ru/student_study/campus_pass/"
                val retrofit = Retrofit.Builder().baseUrl(url1).client(okHttpClient).build()
                val service = retrofit.create(APIService::class.java)
                val response = service.basePage()
                if (response.isSuccessful) {
                    val parsedData = ResponseParser().parseCampusPass(response.body())
                    delay(1000)
                    campusPassLiveData.postValue(Event.success(parsedData))
                }else{
                    campusPassLiveData.postValue(Event.error())
                }

            } catch (e: Exception) {
                Log.e("campusPassLiveData", ": ${e.stackTraceToString()}")
                campusPassLiveData.postValue(Event.error())
            } catch (e: Error) {
                Log.e("campusPassLiveData", ": ${e.stackTraceToString()}")
                campusPassLiveData.postValue(Event.error())
            } catch (e: Throwable) {
                Log.e("campusPassLiveData", ": ${e.stackTraceToString()}")
                campusPassLiveData.postValue(Event.error())
            } catch (e: JSONException) {
                Log.e("campusPassLiveData", ": ${e.stackTraceToString()}")
                campusPassLiveData.postValue(Event.error())
            }
        }
    }

    fun cancelCampusPass(id: String){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                campusPassLiveData.postValue(Event.loading())
                val url1 = "https://ciu.nstu.ru/student_study/campus_pass/"
                val retrofit = Retrofit.Builder().baseUrl(url1).client(okHttpClient).build()
                val service = retrofit.create(APIService::class.java)
                val map = HashMap<String?, String?>()
                map["cancel_reservation"] = "true"
                map["reservation_id"] = id
                val response = service.postForm(map)
                if (response.isSuccessful) {
                    updateCampusPassData()
                }else{
                    campusPassLiveData.postValue(Event.error())
                }

            } catch (e: Exception) {
                Log.e("campusPassLiveData", ": ${e.stackTraceToString()}")
                campusPassLiveData.postValue(Event.error())
            } catch (e: Error) {
                Log.e("campusPassLiveData", ": ${e.stackTraceToString()}")
                campusPassLiveData.postValue(Event.error())
            } catch (e: Throwable) {
                Log.e("campusPassLiveData", ": ${e.stackTraceToString()}")
                campusPassLiveData.postValue(Event.error())
            } catch (e: JSONException) {
                Log.e("campusPassLiveData", ": ${e.stackTraceToString()}")
                campusPassLiveData.postValue(Event.error())
            }
        }
    }

    fun registerCampusPass(time: CampusPassTime) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                campusPassLiveData.postValue(Event.loading())
                val url1 = "https://ciu.nstu.ru/student_study/campus_pass/"
                val retrofit = Retrofit.Builder().baseUrl(url1).client(okHttpClient).build()
                val service = retrofit.create(APIService::class.java)
                val map = HashMap<String?, String?>()
                map["make_reservation"] = "true"
                map["calendar_date"] = selectedDate.value?.data_calendar_date
                map["interval_start"] = time.data_interval_start
                map["interval_id"] = time.data_interval
                val response = service.postForm(map)
                if (response.isSuccessful) {
                    updateCampusPassData()
                }else{
                    campusPassLiveData.postValue(Event.error())
                }

            } catch (e: Exception) {
                Log.e("campusPassLiveData", ": ${e.stackTraceToString()}")
                campusPassLiveData.postValue(Event.error())
            } catch (e: Error) {
                Log.e("campusPassLiveData", ": ${e.stackTraceToString()}")
                campusPassLiveData.postValue(Event.error())
            } catch (e: Throwable) {
                Log.e("campusPassLiveData", ": ${e.stackTraceToString()}")
                campusPassLiveData.postValue(Event.error())
            } catch (e: JSONException) {
                Log.e("campusPassLiveData", ": ${e.stackTraceToString()}")
                campusPassLiveData.postValue(Event.error())
            }
        }
    }
}