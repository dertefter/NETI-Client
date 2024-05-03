package com.dertefter.neticore.NETICoreClient.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dertefter.neticore.NETICoreClient.ResponseParser
import com.dertefter.neticore.api.APIService
import com.dertefter.neticore.data.Event
import com.dertefter.neticore.data.User
import com.dertefter.neticore.data.messages.Message
import com.dertefter.neticore.data.messages.SenderPerson
import com.dertefter.neticore.local.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONException
import retrofit2.Retrofit


class MoneyViewModel(
    application: Application,
    val appPreferences: AppPreferences,
    var okHttpClient: OkHttpClient
) : AndroidViewModel(application) {

    val yearsLiveData = MutableLiveData<Event<List<String>>>()
    val moneyLiveData = MutableLiveData<Event<Map<String, String>>>()

    fun updateYears(){
        val emptyMap = mapOf<String, String>()
        moneyLiveData.postValue(Event.success(emptyMap))
        yearsLiveData.postValue(Event.loading())
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url1 = "https://ciu.nstu.ru/student_study/personal/money/"
                val retrofit = Retrofit.Builder().baseUrl(url1).client(okHttpClient).build()
                val service = retrofit.create(APIService::class.java)
                val response = service.basePage()
                if (response.isSuccessful){
                    val parsedData = ResponseParser().parseMoneyYears(response.body())
                    yearsLiveData.postValue(Event.success(parsedData))
                }else{
                    yearsLiveData.postValue(Event.error())
                }

            } catch (e: Exception) {
                Log.e("moneyViewModel", e.stackTraceToString())
                yearsLiveData.postValue(Event.error())
            } catch (e: Error) {
                Log.e("moneyViewModel", e.stackTraceToString())
                yearsLiveData.postValue(Event.error())
            } catch (e: Throwable) {
                Log.e("moneyViewModel", e.stackTraceToString())
                yearsLiveData.postValue(Event.error())
            } catch (e: JSONException) {
                Log.e("moneyViewModel", e.stackTraceToString())
                yearsLiveData.postValue(Event.error())
            }
        }
    }

    fun loadYearData(year: String){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url1 = "https://ciu.nstu.ru/student_study/personal/money/"
                val retrofit = Retrofit.Builder().baseUrl(url1).client(okHttpClient).build()
                val service = retrofit.create(APIService::class.java)
                val params: HashMap<String?, String?> = HashMap()
                params["year"] = year
                params["month"] = "0"
                val response = service.postForm(params)
                if (response.isSuccessful){
                    val parsedData = ResponseParser().parseMoneyData(response.body())
                    var map = moneyLiveData.value?.data?.toMutableMap()
                    if (map == null){
                        map = mutableMapOf()
                    }
                    map[year] = parsedData
                    moneyLiveData.postValue(Event.success(map))
                }else{
                    var map = moneyLiveData.value?.data?.toMutableMap()
                    if (map == null){
                        map = mutableMapOf()
                    }
                    map[year] = "error"
                    moneyLiveData.postValue(Event.success(map))
                }

            } catch (e: Exception) {
                Log.e("moneyViewModel", e.stackTraceToString())
                moneyLiveData.postValue(Event.error())
            } catch (e: Error) {
                Log.e("moneyViewModel", e.stackTraceToString())
                moneyLiveData.postValue(Event.error())
            } catch (e: Throwable) {
                Log.e("moneyViewModel", e.stackTraceToString())
                moneyLiveData.postValue(Event.error())
            } catch (e: JSONException) {
                Log.e("moneyViewModel", e.stackTraceToString())
                moneyLiveData.postValue(Event.error())
            }
        }
    }
}