package com.dertefter.neticore.NETICoreClient.dispace.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.dertefter.neticore.api.APIService
import com.dertefter.neticore.data.AuthorizationState
import com.dertefter.neticore.local.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONException
import retrofit2.Retrofit

class DiAuthorizationStateViewModel(
    application: Application,
    val appPreferences: AppPreferences,
    var okHttpClient: OkHttpClient
) : AndroidViewModel(application) {

    val uiState = MutableStateFlow(AuthorizationState.UNAUTHORIZED)

    private fun setAuthorized(){
        uiState.value = AuthorizationState.AUTHORIZED
    }

    fun setUnauthorized(){
        uiState.value = AuthorizationState.UNAUTHORIZED
    }

    private fun setAuthorizationError(){
        uiState.value = AuthorizationState.AUTHORIZED_WITH_ERROR
    }

    private fun setLoading(){
        uiState.value = AuthorizationState.LOADING
    }

    fun tryAuthorize(){
        setLoading()
        CoroutineScope(Dispatchers.IO).launch {
        try{
            val url1 = "https://dispace.edu.nstu.ru/user/"
            val retrofit = Retrofit.Builder().baseUrl(url1).client(okHttpClient).build()
            val service = retrofit.create(APIService::class.java)
            val response = service.authDispace("openam", "auth")
            if (response.isSuccessful){
                setAuthorized()
                }
            else{
                    setAuthorizationError()
            }

        } catch (e: Exception){
            Log.e("tryAuthorize", e.stackTraceToString())
            setAuthorizationError()
        } catch (e: Error){
            Log.e("tryAuthorize", e.stackTraceToString())
            setAuthorizationError()
        } catch (e: Throwable){
            Log.e("tryAuthorize", e.stackTraceToString())
            setAuthorizationError()
        }catch (e: JSONException){
            Log.e("tryAuthorize", e.stackTraceToString())
            setAuthorizationError()
        }
        }


    }

}