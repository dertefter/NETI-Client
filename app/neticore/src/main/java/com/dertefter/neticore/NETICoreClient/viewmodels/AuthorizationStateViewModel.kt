package com.dertefter.neticore.NETICoreClient.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.dertefter.neticore.NETICoreClient.NETICoreClient
import com.dertefter.neticore.api.APIService
import com.dertefter.neticore.data.AuthorizationState
import com.dertefter.neticore.local.AppPreferences
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.CookieJar
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit

class AuthorizationStateViewModel(
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

    fun tryAuthorize(login: String, password: String){
        setLoading()
        CoroutineScope(Dispatchers.IO).launch {
        try{
            val url1 = "https://login.nstu.ru/"
            val retrofit = Retrofit.Builder().baseUrl(url1).client(okHttpClient).build()
            val service = retrofit.create(APIService::class.java)
            val jsonObjectString =
                "{\"authId\":\"eyAidHlwIjogIkpXVCIsICJhbGciOiAiSFMyNTYiIH0.eyAib3RrIjogInR2ZW9yazY0dHU5aDc5dTRtb2xoZTBrb3NkIiwgInJlYWxtIjogIm89bG9naW4sb3U9c2VydmljZXMsZGM9b3BlbmFtLGRjPWNpdSxkYz1uc3R1LGRjPXJ1IiwgInNlc3Npb25JZCI6ICJBUUlDNXdNMkxZNFNmY3dIV1l6elZqbTdlbjREYXptS2ZfQktXLTA0UGR1M0lMay4qQUFKVFNRQUNNRElBQWxOTEFCTTJNamc0T0RrM05qUXpNVFE1TXpJMk56TTUqIiB9.iQ7F98fLLFrcDlSI5kYU14d9_Dg9lKN5meoGYIdXxcA\",\"template\":\"\",\"stage\":\"JDBCExt1\",\"header\":\"Авторизация\",\"callbacks\":[{\"type\":\"NameCallback\",\"output\":[{\"name\":\"prompt\",\"value\":\"Логин:\"}],\"input\":[{\"name\":\"IDToken1\",\"value\":\"$login\"}]},{\"type\":\"PasswordCallback\",\"output\":[{\"name\":\"prompt\",\"value\":\"Пароль:\"}],\"input\":[{\"name\":\"IDToken2\",\"value\":\"$password\"}]}]}"
            val requestBody = jsonObjectString.toRequestBody("application/json".toMediaTypeOrNull())

                val response = service.authPart1(requestBody)
                if (response.isSuccessful){
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val prettyJson = gson.toJson(JsonParser.parseString(response.body()?.string()))
                    Log.e("babababababababa", prettyJson)
                    val Jobject = JSONObject(prettyJson)
                    Log.e("bababab", Jobject.toString())
                    val tokenId = Jobject.getString("tokenId")
                    if (!tokenId.isNullOrEmpty()){
                        appPreferences.token = tokenId
                        appPreferences.login = login
                        appPreferences.password = password
                        withContext(Dispatchers.Main){
                            setAuthorized()
                        }
                    }else{
                        setAuthorizationError()
                    }
                }else{
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