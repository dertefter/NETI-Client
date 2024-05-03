package com.dertefter.neticore.NETICoreClient.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dertefter.neticore.NETICoreClient.ResponseParser
import com.dertefter.neticore.api.APIService
import com.dertefter.neticore.data.Event
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.User
import com.dertefter.neticore.local.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONException
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class UserInfoViewModel(
    application: Application,
    val appPreferences: AppPreferences,
    var okHttpClient: OkHttpClient
) : AndroidViewModel(application) {
    val app = application
    val userInfoLiveData = MutableLiveData<Event<User>>()

    val userPhotoLiveData = MutableLiveData<Event<String>>()

    fun updateUserData(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url1 = "https://ciu.nstu.ru/student_study/personal/contact_info/"
                val retrofit = Retrofit.Builder().baseUrl(url1).client(okHttpClient).build()
                val service = retrofit.create(APIService::class.java)
                val response = service.basePage()
                if (response.isSuccessful) {
                    val parsedData = ResponseParser().parseUserInfo(response.body())
                    if (parsedData != null){
                        userInfoLiveData.postValue(Event.success(parsedData))
                        if (userPhotoLiveData.value?.status!= Status.SUCCESS){
                            updateProfilePhoto()
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

    fun updateProfilePhoto(){
        CoroutineScope(Dispatchers.IO).launch {
            userPhotoLiveData.postValue(Event.loading())
            try {
                val url1 = "https://ciu.nstu.ru/student_study/personal/photo_pass/"
                val retrofit = Retrofit.Builder().baseUrl(url1).client(okHttpClient).build()
                val service = retrofit.create(APIService::class.java)
                val response = service.test_download()
                if (response.isSuccessful) {
                    saveFile(response.body(), app.cacheDir.absolutePath+"/photo_pass.jpg")
                }else{
                    userPhotoLiveData.postValue(Event.error())
                }

            } catch (e: Exception) {
                userPhotoLiveData.postValue(Event.error())
                Log.e("photopass", e.stackTraceToString())
            } catch (e: Error) {
                userPhotoLiveData.postValue(Event.error())
                Log.e("photopass", e.stackTraceToString())
            } catch (e: Throwable) {
                userPhotoLiveData.postValue(Event.error())
                Log.e("photopass", e.stackTraceToString())
            } catch (e: JSONException) {
                userPhotoLiveData.postValue(Event.error())
                Log.e("photopass", e.stackTraceToString())
            }
        }
    }

    fun setError(){
        userInfoLiveData.postValue(Event.error())
    }

    fun saveFile(body: ResponseBody?, pathWhereYouWantToSaveFile: String):String{
        //delete file if exist
        val file = File(pathWhereYouWantToSaveFile)
        if (file.exists()){
            file.delete()
        }
        if (body==null)
            return ""
        var input: InputStream? = null
        try {
            input = body.byteStream()
            val fos = FileOutputStream(pathWhereYouWantToSaveFile)
            fos.use { output ->
                val buffer = ByteArray(4 * 1024) // or other buffer size
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
            userPhotoLiveData.postValue(Event.success(pathWhereYouWantToSaveFile))
            return pathWhereYouWantToSaveFile
        } catch (e:Exception){
            userPhotoLiveData.postValue(Event.error())
            Log.e("saveFile",e.toString())
        }
        finally {
            input?.close()
        }
        return ""
    }
}