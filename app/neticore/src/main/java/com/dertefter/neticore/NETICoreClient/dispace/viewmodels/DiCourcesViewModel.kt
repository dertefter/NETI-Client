package com.dertefter.neticore.NETICoreClient.dispace.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dertefter.neticore.NETICoreClient.ResponseParser
import com.dertefter.neticore.api.APIService
import com.dertefter.neticore.data.Event
import com.dertefter.neticore.data.dispace.di_cources.DiCourse
import com.dertefter.neticore.data.dispace.di_cources.DiCourseView
import com.dertefter.neticore.data.dispace.di_messages.DiMessage
import com.dertefter.neticore.data.dispace.di_messages.DiMessagesData
import com.dertefter.neticore.data.dispace.di_messages.DiSenderPerson
import com.dertefter.neticore.local.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONException
import retrofit2.Retrofit


class DiCourcesViewModel(
    application: Application,
    val appPreferences: AppPreferences,
    var okHttpClient: OkHttpClient
) : AndroidViewModel(application) {

    val courseListLiveData = MutableLiveData<Event<List<DiCourse>>>()
    val favCourseListLiveData = MutableLiveData<Event<List<DiCourse>>>()
    val courseViewLiveData = MutableLiveData<Event<DiCourseView>>()

    fun searchCourse(name: String, page: String){
        courseListLiveData.value = Event.loading()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var retrofit = Retrofit.Builder()
                    .baseUrl("https://dispace.edu.nstu.ru/didesk/index/")
                    .client(okHttpClient)
                    .build()
                val service = retrofit.create(APIService::class.java)
                val params = HashMap<String?, String?>()
                params["faculty_id"] = "-1"
                params["kafedra_id"] = "-1"
                params["year_created"] = "-1"
                params["name"] = name
                params["author_name"] = name
                params["page"] = page
                params["search_by_author_or_course"] = "OR"
                params["is_ajax"] = "false"
                params["action"] = "list"

                val response = service.getCourses(params)
                if (response.isSuccessful){
                    val parsedData = ResponseParser().parseDiCourseList(response.body())
                    Log.e("Zzzzzzzzzzz", "parsedData: $parsedData")
                    courseListLiveData.postValue(Event.success(parsedData))
                }else{
                    courseListLiveData.postValue(Event.error())
                }

            } catch (e: Exception) {
                Log.e("diCoursrViewModel", e.stackTraceToString())
                courseListLiveData.postValue(Event.error())
            } catch (e: Error) {
                Log.e("diCoursrViewModel", e.stackTraceToString())
                courseListLiveData.postValue(Event.error())
            } catch (e: Throwable) {
                Log.e("diCoursrViewModel", e.stackTraceToString())
                courseListLiveData.postValue(Event.error())
            } catch (e: JSONException) {
                Log.e("diCoursrViewModel", e.stackTraceToString())
                courseListLiveData.postValue(Event.error())
            }
        }
    }

    fun updateFav(page: Int){
        favCourseListLiveData.postValue(Event.loading())
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var retrofit = Retrofit.Builder()
                    .baseUrl("https://dispace.edu.nstu.ru/didesk/index/")
                    .client(okHttpClient)
                    .build()
                val service = retrofit.create(APIService::class.java)
                val params = HashMap<String?, String?>()
                params["page"] = page.toString()
                params["is_ajax"] = "true"
                params["faculty_id"] = "favorites"
                val response = service.getCourses(params)
                if (response.isSuccessful){
                    val parsedData = ResponseParser().parseDiCourseList(response.body())
                    favCourseListLiveData.postValue(Event.success(parsedData))
                }else{
                    favCourseListLiveData.postValue(Event.error())
                }

            } catch (e: Exception) {
                Log.e("diCoursrViewModel", e.stackTraceToString())
                favCourseListLiveData.postValue(Event.error())
            } catch (e: Error) {
                Log.e("diCoursrViewModel", e.stackTraceToString())
                favCourseListLiveData.postValue(Event.error())
            } catch (e: Throwable) {
                Log.e("diCoursrViewModel", e.stackTraceToString())
                favCourseListLiveData.postValue(Event.error())
            } catch (e: JSONException) {
                Log.e("diCoursrViewModel", e.stackTraceToString())
                favCourseListLiveData.postValue(Event.error())
            }
        }
    }

    fun addFav(id: String){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var retrofit = Retrofit.Builder()
                    .baseUrl("https://dispace.edu.nstu.ru/didesk/course/proceed/")
                    .client(okHttpClient)
                    .build()
                val params = HashMap<String?, String?>()
                params["action"] = "add_favourites"
                params["course_id"] = id
                val service = retrofit.create(APIService::class.java)
                val response = service.getCourses(params)
                if (response.isSuccessful){
                    val ld1 = courseListLiveData.value?.data
                    val ld2 = favCourseListLiveData.value?.data as MutableList<DiCourse>
                    if (!ld1.isNullOrEmpty()){
                        val course = ld1.find { it.id == id }
                        if (course != null){
                            course.infav = 1
                            courseListLiveData.postValue(Event.success(ld1))
                        }
                    }

                    if (!ld2.isNullOrEmpty()){
                        val course = ld2.find { it.id == id }
                        if (course != null){
                            course.infav = 1
                        }else{
                            updateFav(1)
                        }
                        favCourseListLiveData.postValue(Event.success(ld2))
                    }else{
                        updateFav(1)
                    }

                }else{
                    favCourseListLiveData.postValue(Event.error())
                }

            } catch (e: Exception) {
                Log.e("diCoursrViewModel", e.stackTraceToString())
                favCourseListLiveData.postValue(Event.error())
            } catch (e: Error) {
                Log.e("diCoursrViewModel", e.stackTraceToString())
                favCourseListLiveData.postValue(Event.error())
            } catch (e: Throwable) {
                Log.e("diCoursrViewModel", e.stackTraceToString())
                favCourseListLiveData.postValue(Event.error())
            } catch (e: JSONException) {
                Log.e("diCoursrViewModel", e.stackTraceToString())
                favCourseListLiveData.postValue(Event.error())
            }

        }

    }

    fun removeFav(id: String){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var retrofit = Retrofit.Builder()
                    .baseUrl("https://dispace.edu.nstu.ru/didesk/course/proceed/")
                    .client(okHttpClient)
                    .build()
                val params = HashMap<String?, String?>()
                params["action"] = "delete_favourites"
                params["course_id"] = id
                val service = retrofit.create(APIService::class.java)
                val response = service.getCourses(params)
                if (response.isSuccessful){
                    val ld1 = courseListLiveData.value?.data
                    val ld2 = favCourseListLiveData.value?.data as MutableList<DiCourse>
                    if (!ld1.isNullOrEmpty()){
                        val course = ld1.find { it.id == id }
                        if (course != null){
                            course.infav = 0
                            courseListLiveData.postValue(Event.success(ld1))
                        }
                    }

                    if (!ld2.isNullOrEmpty()){
                        val course = ld2.find { it.id == id }
                        if (course != null){
                            ld2.remove(course)
                            favCourseListLiveData.postValue(Event.success(ld2))
                        }else{
                            updateFav(1)
                        }
                    }else{
                        updateFav(1)
                    }
                }else{
                    favCourseListLiveData.postValue(Event.error())
                }

            } catch (e: Exception) {
                Log.e("diCoursrViewModel", e.stackTraceToString())
                favCourseListLiveData.postValue(Event.error())
            } catch (e: Error) {
                Log.e("diCoursrViewModel", e.stackTraceToString())
                favCourseListLiveData.postValue(Event.error())
            } catch (e: Throwable) {
                Log.e("diCoursrViewModel", e.stackTraceToString())
                favCourseListLiveData.postValue(Event.error())
            } catch (e: JSONException) {
                Log.e("diCoursrViewModel", e.stackTraceToString())
                favCourseListLiveData.postValue(Event.error())
            }

        }

    }


    fun updateCourseView(id: String, page: String? = null){
        courseViewLiveData.value = Event.loading()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = if (!page.isNullOrEmpty()){
                    "https://dispace.edu.nstu.ru/didesk/course/show/$id/$page/"
                }else{
                    "https://dispace.edu.nstu.ru/didesk/course/show/$id/"
                }
                val retrofit = Retrofit.Builder()
                    .baseUrl(url)
                    .client(okHttpClient)
                    .build()
                val service = retrofit.create(APIService::class.java)
                val response = service.readCourse()
                if (response.isSuccessful){
                    val parsedData = ResponseParser().parseCourseView(response.body())
                    parsedData.id = id
                    courseViewLiveData.postValue(Event.success(parsedData))
                }else{
                    courseViewLiveData.postValue(Event.error())
                }

            } catch (e: Exception) {
                Log.e("diCoursrViewModel", e.stackTraceToString())
                courseViewLiveData.postValue(Event.error())
            } catch (e: Error) {
                Log.e("diCoursrViewModel", e.stackTraceToString())
                courseViewLiveData.postValue(Event.error())
            } catch (e: Throwable) {
                Log.e("diCoursrViewModel", e.stackTraceToString())
                courseViewLiveData.postValue(Event.error())
            } catch (e: JSONException) {
                Log.e("diCoursrViewModel", e.stackTraceToString())
                courseViewLiveData.postValue(Event.error())
            }
        }
    }
}