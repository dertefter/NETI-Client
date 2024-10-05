package com.dertefter.neticore.NETICoreClient.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dertefter.neticore.NETICoreClient.ResponseParser
import com.dertefter.neticore.api.APIService
import com.dertefter.neticore.data.Event
import com.dertefter.neticore.data.User
import com.dertefter.neticore.data.e_library.ELibrarySearchItem
import com.dertefter.neticore.data.messages.Message
import com.dertefter.neticore.data.messages.SenderPerson
import com.dertefter.neticore.local.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONException
import retrofit2.Retrofit


class ELibraryViewModel(
    application: Application,
    val appPreferences: AppPreferences,
    var okHttpClient: OkHttpClient
) : AndroidViewModel(application) {

    val searchItemsLiveData = MutableLiveData<Event<List<ELibrarySearchItem>>>()

    fun search(title: String){
        searchItemsLiveData.postValue(Event.loading())
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url1 = "https://ciu.nstu.ru/student_study/ecources/e-library_search/"
                val retrofit = Retrofit.Builder().baseUrl(url1).client(okHttpClient).build()
                val service = retrofit.create(APIService::class.java)
                val params: HashMap<String?, String?> = HashMap()
                params["search_action"] = "FIND"
                params["title"] = title
                params["source_type"] = "0"
                params["title_mode"] = "2"
                val response = service.postForm(params)
                if (response.isSuccessful){
                    val parsedData = ResponseParser().parseELibrarySearch(response.body())
                    searchItemsLiveData.postValue(Event.success(parsedData))
                }else{
                    searchItemsLiveData.postValue(Event.error())
                }

            } catch (e: Exception) {
                Log.e("elibraryViewModel", e.stackTraceToString())
                searchItemsLiveData.postValue(Event.error())
            } catch (e: Error) {
                Log.e("elibraryViewModel", e.stackTraceToString())
                searchItemsLiveData.postValue(Event.error())
            } catch (e: Throwable) {
                Log.e("elibraryViewModel", e.stackTraceToString())
                searchItemsLiveData.postValue(Event.error())
            } catch (e: JSONException) {
                Log.e("elibraryViewModel", e.stackTraceToString())
                searchItemsLiveData.postValue(Event.error())
            }
        }
    }
}