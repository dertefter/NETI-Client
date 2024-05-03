package com.dertefter.neticore.NETICoreClient.dispace.filemanager

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.dertefter.neticore.api.APIService
import com.dertefter.neticore.local.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class FileManagerViewModel(
    application: Application,
    appPreferences: AppPreferences,
    okHttpClient: OkHttpClient
) {

    var okHttpClient: OkHttpClient? = null
    val app = application
    val savedFilesListLiveData = MutableLiveData<List<String>>()

    fun updateSavedFilesList(){
        val filesDirListFiles = File(app.filesDir.absolutePath).listFiles()
        if (filesDirListFiles != null) {
            if ("down" !in filesDirListFiles.toString()){
                File(app.filesDir.absolutePath+"/down").mkdir()
            }
        }else{
            File(app.filesDir.absolutePath+"/down").mkdir()
        }


        val files = File(app.filesDir.absolutePath+"/down/").listFiles()
        val filesList = mutableListOf<String>()
        if (files != null){
            for (file in files){
                filesList.add(file.name)
            }
        }
        savedFilesListLiveData.postValue(filesList)
    }

    fun deleteFile(path: String){
        val file = File(app.filesDir.absolutePath+"/down/"+path)
        file.delete()
        updateSavedFilesList()
    }

    fun downloadFile(link: String, _name: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(link)
            .client(okHttpClient)
            .build()
        val service = retrofit.create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.downloadCourseFile().body()
            val filesList = File(app.filesDir.absolutePath).listFiles()
            if (filesList != null) {
                if ("down" !in filesList.toString()) {
                    File(app.filesDir.absolutePath + "/down").mkdir()
                }
            } else {
                File(app.filesDir.absolutePath + "/down").mkdir()
            }


            val pathWhereYouWantToSaveFile = app.filesDir.absolutePath + "/down/" + _name
            saveFile(response, pathWhereYouWantToSaveFile)


        }

    }

    fun saveFile(body: ResponseBody?, pathWhereYouWantToSaveFile: String):String{
        if (body==null)
            return ""
        var input: InputStream? = null
        try {
            input = body.byteStream()
            //val file = File(getCacheDir(), "cacheFileAppeal.srl")
            val fos = FileOutputStream(pathWhereYouWantToSaveFile)
            fos.use { output ->
                val buffer = ByteArray(4 * 1024) // or other buffer size
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
            updateSavedFilesList()
            return pathWhereYouWantToSaveFile
        } catch (e:Exception){
            Log.e("saveFile",e.toString())
        }
        finally {
            input?.close()
        }
        return ""
    }

}