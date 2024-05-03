package com.dertefter.neticore.NETICoreClient.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dertefter.neticore.NETICoreClient.ResponseParser
import com.dertefter.neticore.api.APIService
import com.dertefter.neticore.data.Event
import com.dertefter.neticore.data.User
import com.dertefter.neticore.data.news.News
import com.dertefter.neticore.data.news.NewsContent
import com.dertefter.neticore.local.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONException
import retrofit2.Retrofit

class NewsViewModel(
    application: Application,
    val appPreferences: AppPreferences,
    var okHttpClient: OkHttpClient
) : AndroidViewModel(application) {

    val newsListLiveData = MutableLiveData<Event<List<News>>>()
    val newsContentLiveData = MutableLiveData<Event<NewsContent>>()
    var page = 0

    fun getNewsList(isNeedClear: Boolean = false){
        if (isNeedClear){
            page = 0
        }
        page += 1
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://www.nstu.ru/")
                    .build()
                val service = retrofit.create(APIService::class.java)
                val response = service.getNews(page.toString())

                if (response.isSuccessful) {
                    val parsedData = ResponseParser().parseNews(response.body())
                    if (parsedData != null){
                        if (isNeedClear){
                            newsListLiveData.postValue(Event.success(parsedData))
                        }else{
                            val currentNewsList = newsListLiveData.value?.data
                            if (currentNewsList != null){
                                val newList = currentNewsList.toMutableList()
                                newList.addAll(parsedData)
                                newsListLiveData.postValue(Event.success(newList))
                            }else{
                                newsListLiveData.postValue(Event.success(parsedData))
                            }
                        }

                    }

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
        newsListLiveData.postValue(Event.error())
    }

    fun setErrorContent(){
        newsContentLiveData.postValue(Event.error())
    }

    fun readNews(newsId: String) {
        newsContentLiveData.postValue(Event.loading())
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://www.nstu.ru/")
                    .build()
                val service = retrofit.create(APIService::class.java)
                val response = service.readNews(newsId)
                if (response.isSuccessful) {
                    val parsedData = ResponseParser().parseNewsContent(response.body())
                    if (parsedData != null){
                        newsContentLiveData.postValue(Event.success(parsedData))
                    }else{
                        setErrorContent()
                    }

                }

            } catch (e: Exception) {
                setErrorContent()
            } catch (e: Error) {
                setErrorContent()
            } catch (e: Throwable) {
                setErrorContent()
            } catch (e: JSONException) {
                setErrorContent()
            }
        }
    }


}