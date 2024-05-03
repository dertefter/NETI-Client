package com.dertefter.neticore.NETICoreClient.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dertefter.neticore.NETICoreClient.ResponseParser
import com.dertefter.neticore.api.APIService
import com.dertefter.neticore.data.Event
import com.dertefter.neticore.data.Person
import com.dertefter.neticore.data.User
import com.dertefter.neticore.local.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONException
import retrofit2.Retrofit

class SearchPersonViewModel(
    application: Application,
    val appPreferences: AppPreferences,
    var okHttpClient: OkHttpClient
) : AndroidViewModel(application) {

    val personListLiveData = MutableLiveData<Event<List<Person>>>()

    fun updatePersonList(search_term: String, page: Int = 1){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "https://www.nstu.ru/"
                val retrofit = Retrofit.Builder().baseUrl(url).build()
                val service = retrofit.create(APIService::class.java)
                val response = service.findPerson(search_term, page.toString())
                if (response.isSuccessful) {
                    val parsedData = ResponseParser().parsePersonList(response.body())
                    personListLiveData.postValue(Event.success(parsedData))
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

    fun setError(){
        personListLiveData.postValue(Event.error())
    }


}