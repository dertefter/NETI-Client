package com.dertefter.neticore.NETICoreClient.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dertefter.neticore.NETICoreClient.ResponseParser
import com.dertefter.neticore.api.APIService
import com.dertefter.neticore.data.Event
import com.dertefter.neticore.data.sessia_results.SessiaResults
import com.dertefter.neticore.local.AppPreferences
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Retrofit



class SessiaResultsViewModel(
    application: Application,
    val appPreferences: AppPreferences,
    var okHttpClient: OkHttpClient
) : AndroidViewModel(application) {

    val sessiaResultsLiveData = MutableLiveData<Event<List<SessiaResults>>>()

    val sessiaLinkLiveData = MutableLiveData<Event<String>>()

    fun updateSessiaResults(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                sessiaResultsLiveData.postValue(Event.loading())
                val url1 = "https://ciu.nstu.ru/student_study/student_info/progress/"
                val retrofit = Retrofit.Builder().baseUrl(url1).client(okHttpClient).build()
                val service = retrofit.create(APIService::class.java)
                val response = service.basePage()
                if (response.isSuccessful) {
                    val parsedData = ResponseParser().parseSessiaResults(response.body())
                   sessiaResultsLiveData.postValue(Event.success(parsedData))
               }else{
                   setError()
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


    fun getLink(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                sessiaResultsLiveData.postValue(Event.loading())
                val url1 = "https://ciu.nstu.ru/student_study/student_info/link_progress/"
                val retrofit = Retrofit.Builder().baseUrl(url1).client(okHttpClient).build()
                val service = retrofit.create(APIService::class.java)
                val response = service.basePage()
                if (response.isSuccessful) {
                    val pretty = response.body()?.string().toString()
                    val doc: Document = Jsoup.parse(pretty)
                    val link = doc.body().select("textarea").first()?.ownText().toString()
                    sessiaLinkLiveData.postValue(Event.success(link))
                }

            } catch (e: Exception) {
                Log.e("SessiaViewModel", "getLink: ${e.stackTraceToString()}")
                sessiaLinkLiveData.postValue(Event.error())
            } catch (e: Error) {
                Log.e("SessiaViewModel", "getLink: ${e.stackTraceToString()}")
                sessiaLinkLiveData.postValue(Event.error())
            } catch (e: Throwable) {
                Log.e("SessiaViewModel", "getLink: ${e.stackTraceToString()}")
                sessiaLinkLiveData.postValue(Event.error())
            } catch (e: JSONException) {
                Log.e("SessiaViewModel", "getLink: ${e.stackTraceToString()}")
                sessiaLinkLiveData.postValue(Event.error())
            }
        }
    }

    fun updateLink(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                sessiaLinkLiveData.postValue(Event.loading())
                val url1 = "https://ciu.nstu.ru/student_study/student_info/link_progress/"
                val retrofit = Retrofit.Builder().baseUrl(url1).client(okHttpClient).build()
                val service = retrofit.create(APIService::class.java)
                val params = HashMap<String?, String?>()
                params["generate_access_url"] = "true"
                val response = service.postForm(params)
                if (response.isSuccessful) {
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val prettyJson = gson.toJson(JsonParser.parseString(response.body()?.string()))
                    val json = JSONObject(prettyJson)
                    val link = json.getString("access_url")
                    sessiaLinkLiveData.postValue(Event.success(link))
                }

            } catch (e: Exception) {
                Log.e("SessiaViewModel", "updateLink: ${e.stackTraceToString()}")
                sessiaLinkLiveData.postValue(Event.error())
            } catch (e: Error) {
                Log.e("SessiaViewModel", "updateLink: ${e.stackTraceToString()}")
                sessiaLinkLiveData.postValue(Event.error())
            } catch (e: Throwable) {
                Log.e("SessiaViewModel", "updateLink: ${e.stackTraceToString()}")
                sessiaLinkLiveData.postValue(Event.error())
            } catch (e: JSONException) {
                Log.e("SessiaViewModel", "updateLink: ${e.stackTraceToString()}")
                sessiaLinkLiveData.postValue(Event.error())
            }
        }
    }

    fun setError(){
        sessiaResultsLiveData.postValue(Event.error())
    }


}