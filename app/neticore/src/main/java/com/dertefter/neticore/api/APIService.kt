package com.dertefter.neticore.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*


interface APIService {
    @GET(".")
    suspend fun getNews(@Query("main_events") page: String?): Response<ResponseBody>

    @FormUrlEncoded
    @POST("cgi-bin/koha/opac-user.pl")
    suspend fun authBooks(@FieldMap params: HashMap<String?, String?>): Response<ResponseBody>

    @FormUrlEncoded
    @POST("cgi-bin/koha/opac-memberentry.pl")
    suspend fun getLibraryData(@FieldMap params: HashMap<String?, String?>): Response<ResponseBody>

    @POST("ssoservice/json/authenticate")
    suspend fun authPart1(@Body requestBody: RequestBody): Response<ResponseBody>

    @POST("json/ido/users?_action=idFromSession")
    suspend fun authPart2(@Body requestBody: RequestBody): Response<ResponseBody>

    @GET(".")
    suspend fun authPart3(): Response<ResponseBody>

    @POST("json/users?_action=validateGoto")
    suspend fun authPart4(@Query("_action") action: String?, @Body requestBody: RequestBody): Response<ResponseBody>

    @GET(".")
    suspend fun basePage(): Response<ResponseBody>

    @GET(".")
    suspend fun docRemovable(@Query("id") id: String?): Response<ResponseBody>

    @FormUrlEncoded
    @POST(".")
    suspend fun docRemove(@Query("id") id: String?, @FieldMap params: HashMap<String?, String?>): Response<ResponseBody>

    @FormUrlEncoded
    @POST(".")
    suspend fun postForm(@FieldMap params: HashMap<String?, String?>): Response<ResponseBody>

    @GET("mess_teacher")
    suspend fun messages(@Query("year") year: String?): Response<ResponseBody>

    @GET("schedule")
    suspend fun getWeekList(@Query("group") group: String?): Response<ResponseBody>

    @GET("news/news_more")
    suspend fun readNews(@Query("idnews") idnews: String?): Response<ResponseBody>

    @GET("student_study/mess_teacher/view")
    suspend fun readMes(@Query("id") group: String?): Response<ResponseBody>

    @GET("student_study/campus_pass")
    suspend fun readCampusPass(): Response<ResponseBody>

    @GET("phone/object")
    suspend fun findPerson(@Query("search_term") search_term: String?, @Query("page") page: String?): Response<ResponseBody>

    @GET("proceed")
    suspend fun authDispace(@Query("login") login: String?, @Query("password") password: String?): Response<ResponseBody>

    @FormUrlEncoded
    @POST(".")
    suspend fun getDispaceMessages(@FieldMap params: HashMap<String?, String?>): Response<ResponseBody>

    @FormUrlEncoded
    @POST(".")
    suspend fun getDispaceEvents(@FieldMap params: HashMap<String?, String?>): Response<ResponseBody>

    @GET(".")
    suspend fun timetablePerson(): Response<ResponseBody>

    @GET("/studies/schedule/schedule_classes/schedule")
    suspend fun getGroup(@Query("group") page: String?): Response<ResponseBody>

    @FormUrlEncoded
    @POST(".")
    suspend fun getCourses(@FieldMap params: HashMap<String?, String?>): Response<ResponseBody>

    @GET(".")
    suspend fun readCourse(): Response<ResponseBody>

    @Streaming
    @GET(".")
    suspend fun downloadFile(@Query("action") action: String?, @Query("file") file: String?, @Query("name") name: String?): Response<ResponseBody>

    @Streaming
    @GET(".")
    suspend fun downloadCourseFile(): Response<ResponseBody>

    @GET("studies/schedule/schedule_classes")
    suspend fun getGroupList(@Query("query") group: String): Response<ResponseBody>

    @GET("/studies/schedule/schedule_classes/schedule")
    suspend fun getScheduleGuest(@Query("group") group: String?, @Query("week") week: String?): Response<ResponseBody>

    @Streaming
    @GET(".")
    suspend fun test_download(@Query("what") what: String = "5"): Response<ResponseBody>



}