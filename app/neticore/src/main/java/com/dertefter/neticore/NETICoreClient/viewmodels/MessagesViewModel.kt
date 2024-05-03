package com.dertefter.neticore.NETICoreClient.viewmodels

import android.app.Application
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


class MessagesViewModel(
    application: Application,
    val appPreferences: AppPreferences,
    var okHttpClient: OkHttpClient
) : AndroidViewModel(application) {

    val userInfoLiveData = MutableLiveData<Event<User>>()

    val senderListLiveData1 = MutableLiveData<Event<List<SenderPerson>>>()
    val senderListLiveData2 = MutableLiveData<Event<List<SenderPerson>>>()

    val readMessagesLiveData = MutableLiveData<Event<String>>()

    val deleteMessageTaskLiveData = MutableLiveData<Event<Boolean>>()

    fun updateSenderList(tab: Int){
        val ld = if (tab == 0) senderListLiveData1 else senderListLiveData2
        ld.postValue(Event.loading())
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url1 = "https://ciu.nstu.ru/student_study/"
                val retrofit = Retrofit.Builder().baseUrl(url1).client(okHttpClient).build()
                val service = retrofit.create(APIService::class.java)
                val response = service.messages("-1")
                if (response.isSuccessful){
                    val senderList = ResponseParser().parseSenderList(response.body(), tab)
                    ld.postValue(Event.success(senderList))
                    val senderListWithFullNamesAndAvatars = senderList.toMutableList()
                    if (appPreferences.messages_avatars != true){
                        for (it in senderList){
                            try{
                                CoroutineScope(Dispatchers.IO).launch {
                                    val url = "https://www.nstu.ru/"
                                    val retrofit = Retrofit.Builder().baseUrl(url).build()
                                    val service = retrofit.create(APIService::class.java)
                                    val response = service.findPerson(it.name.split(".")[0], "1")
                                    val parsedData = ResponseParser().parsePersonList(response.body())
                                    if (parsedData.isNotEmpty()){
                                        senderListWithFullNamesAndAvatars[senderList.indexOf(it)].name = parsedData[0].name
                                        senderListWithFullNamesAndAvatars[senderList.indexOf(it)].pic = parsedData[0].pic
                                        senderListWithFullNamesAndAvatars[senderList.indexOf(it)].person = parsedData[0]
                                    }
                                    ld.postValue(Event.success(senderListWithFullNamesAndAvatars))
                                }

                            } catch (e: Exception) {
                            } catch (e: Error) {
                            } catch (e: Throwable) {
                            } catch (e: JSONException) {
                            }

                        }
                    }
                }

            } catch (e: Exception) {
                setErrorForTab(tab)
            } catch (e: Error) {
                setErrorForTab(tab)
            } catch (e: Throwable) {
                setErrorForTab(tab)
            } catch (e: JSONException) {
                setErrorForTab(tab)
            }
        }
    }

    fun readMessage(mesId: String){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                readMessagesLiveData.postValue(Event.loading())
                val url1 = "https://ciu.nstu.ru/"
                val retrofit = Retrofit.Builder().baseUrl(url1).client(okHttpClient).build()
                val service = retrofit.create(APIService::class.java)
                val response = service.readMes(mesId)
                if (response.isSuccessful){
                    val parsedData = ResponseParser().parseMessage(response.body())
                    readMessagesLiveData.postValue(Event.success(parsedData))
                }

            } catch (e: Exception) {
                readMessagesLiveData.postValue(Event.error())
            } catch (e: Error) {
                readMessagesLiveData.postValue(Event.error())
            } catch (e: Throwable) {
                readMessagesLiveData.postValue(Event.error())
            } catch (e: JSONException) {
                readMessagesLiveData.postValue(Event.error())
            }
        }
    }

    fun deleteMessage(mesId: String){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                readMessagesLiveData.postValue(Event.loading())
                val url1 = "https://ciu.nstu.ru/student_study/mess_teacher/ajax_del_mes/"
                val retrofit = Retrofit.Builder().baseUrl(url1).client(okHttpClient).build()
                val service = retrofit.create(APIService::class.java)
                val params = HashMap<String?, String?>()
                params["idmes"] = mesId
                params["what"] = "1"
                params["type"] = "1"
                params["vid_sort"] = "1"
                params["year"] = "-1"
                val response = service.postForm(params)
                if (response.isSuccessful){
                    deleteMessageTaskLiveData.postValue(Event.success(true))
                    updateSenderList(0)
                    updateSenderList(1)
                }else{
                    deleteMessageTaskLiveData.postValue(Event.success(false))
                }
            } catch (e: Exception) {
                readMessagesLiveData.postValue(Event.error())
            } catch (e: Error) {
                readMessagesLiveData.postValue(Event.error())
            } catch (e: Throwable) {
                readMessagesLiveData.postValue(Event.error())
            } catch (e: JSONException) {
                readMessagesLiveData.postValue(Event.error())
            }
        }

    }

    fun setErrorForTab(tab: Int){
        val ld = if (tab == 0) senderListLiveData1 else senderListLiveData2
        ld.postValue(Event.error())
    }

    fun removeIsNewOnMessage(chatItem: Message) {
        val ld1 = senderListLiveData1
        val ld2 = senderListLiveData2
        for (i in ld1.value?.data?.indices!!){
            val messages = ld1.value?.data?.get(i)?.messages
            messages?.find{
                it.mesId == chatItem.mesId
            }?.isNew = false
            ld1.postValue(Event.success(ld1.value?.data))
        }
        for (i in ld2.value?.data?.indices!!){
            val messages = ld2.value?.data?.get(i)?.messages
            messages?.find{
                it.mesId == chatItem.mesId
            }?.isNew = false
            ld2.postValue(Event.success(ld2.value?.data))
        }
    }


}