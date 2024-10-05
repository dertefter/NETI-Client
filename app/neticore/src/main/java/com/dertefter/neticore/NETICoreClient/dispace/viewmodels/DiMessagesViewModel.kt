package com.dertefter.neticore.NETICoreClient.dispace.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dertefter.neticore.NETICoreClient.ResponseParser
import com.dertefter.neticore.api.APIService
import com.dertefter.neticore.data.Event
import com.dertefter.neticore.data.dispace.di_messages.DiMessage
import com.dertefter.neticore.data.dispace.di_messages.DiMessagesData
import com.dertefter.neticore.data.dispace.di_messages.DiSenderPerson
import com.dertefter.neticore.local.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONException
import retrofit2.Retrofit


class DiMessagesViewModel(
    application: Application,
    val appPreferences: AppPreferences,
    var okHttpClient: OkHttpClient
) : AndroidViewModel(application) {

    val chatListLiveData = MutableLiveData<Event<List<DiSenderPerson>>>()

    val messageListLiveData = MutableLiveData<Event<DiMessagesData>>()

    val mesListPage = 1

    fun updateSenderList(page: Int = 1){
        chatListLiveData.postValue(Event.loading())
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var retrofit = Retrofit.Builder()
                    .baseUrl("https://dispace.edu.nstu.ru/diclass/privmsg/dialog/")
                    .client(okHttpClient)
                    .build()
                val service = retrofit.create(APIService::class.java)
                val params = HashMap<String?, String?>()
                params["group"] = "dialogs"
                params["page"] = page.toString()
                params["search"] = ""
                params["action"] = "list"
                val response = service.getDispaceMessages(params)
                if (response.isSuccessful){
                    val parsedData = ResponseParser().parseDiChats(response.body())
                    chatListLiveData.postValue(Event.success(parsedData))
                }

            } catch (e: Exception) {
                Log.e("DiMes Err", e.stackTraceToString())
                chatListLiveData.postValue(Event.error())
            } catch (e: Error) {
                Log.e("DiMes Err", e.stackTraceToString())
                chatListLiveData.postValue(Event.error())
            } catch (e: Throwable) {
                Log.e("DiMes Err", e.stackTraceToString())
                chatListLiveData.postValue(Event.error())
            } catch (e: JSONException) {
                Log.e("DiMes Err", e.stackTraceToString())
                chatListLiveData.postValue(Event.error())
            }
        }
    }

    fun sendmes(mes: String, chatid: String) {
        var retrofit = Retrofit.Builder()
            .baseUrl("https://dispace.edu.nstu.ru/diclass/privmsg/proceed/")
            .client(okHttpClient)
            .build()
        val service = retrofit.create(APIService::class.java)
        val params = HashMap<String?, String?>()
        params["action"] = "send_msgs"
        params["rec_students"] = chatid
        params["message"] = "<p>$mes</p>"
        params["text_signature"] = ""
        params["add_signature"] = "false"
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.getDispaceMessages(params)
            if (response.isSuccessful) {
                withContext(Dispatchers.Main){
                    updateMessageList(1, chatid)
                }
            }
        }
    }

    fun updateMessageList(page: Int = 1, uw_id: String){
        messageListLiveData.value = Event.loading()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var retrofit = Retrofit.Builder()
                    .baseUrl("https://dispace.edu.nstu.ru/diclass/privmsg/messages/")
                    .client(okHttpClient)
                    .build()
                val service = retrofit.create(APIService::class.java)
                val params = HashMap<String?, String?>()
                params["action"] = "dialog"
                params["uw_id"] = uw_id
                params["page"] = page.toString()

                val response = service.getDispaceMessages(params)
                Log.e("DiChat", "${response.isSuccessful}")
                if (response.isSuccessful){
                    val parsedData = ResponseParser().parseDiMessages(response.body())
                    messageListLiveData.postValue(Event.success(parsedData))
                }else{
                }

            } catch (e: Exception) {
                messageListLiveData.postValue(Event.error())
            } catch (e: Error) {
                messageListLiveData.postValue(Event.error())
            } catch (e: Throwable) {
                messageListLiveData.postValue(Event.error())
            } catch (e: JSONException) {
                messageListLiveData.postValue(Event.error())
            }
        }
    }
}