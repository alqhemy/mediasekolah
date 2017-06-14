package com.media.schoolday.api

/*
 * Created by yosi on 19/05/2017.
 */

import com.media.schoolday.models.*
import com.orhanobut.wasp.Callback
import com.orhanobut.wasp.http.*

/*
 * Created by yosi on 03/05/2017.
 */
interface ApiService {
    @POST("/register")
    fun getUser(@BodyMap user: HashMap<String,String>, callback: Callback<ResponUser>)

    @GET("/profile")
    fun getProfile(@Header("Authorization") a: String, callback: Callback<ResponProfile>)

    @GET("/news")
    fun getNews(@Header("Authorization") a: String, callback: Callback<ResponNews>)

    @GET("/sekolah")
    fun getSekolah(callback: Callback<ResponSchool>)

    @POST("/sekolah/siswa")
    fun postSiswa(@Body filter: FilterModel, callback: Callback<Students>)

    @POST("/sekolah/guru")
    fun postGuru(@Body filter: FilterModel, callback: Callback<Teachers>)

    @POST("/user")
    fun postUser(@Body filter: FilterModel, callback: Callback<Teachers>)

    @PUT("/profile/anak")
    fun putProfileAnak(@Header("Authorization") a: String, @Body child: ProfilAnak, callback: Callback<Profile>)

    @PUT("/profile/guru")
    fun putProfileGuru(@Header("Authorization") a: String, @Body guru: ProfileGuru, callback: Callback<Profile>)

    @POST("/news")
    fun postNews(@Header("Authorization") a:String, @Body news: PostNews, callback: Callback<ResponPostNews>)

    @Headers(
        "Accept:image/jpeg, image/png",
        "Content-type:application/json, multipart/form-data"
    )
    @PUT("/news/{id}/photo")
    fun postNewsPhoto(@Path("id") id: String, @BodyMap photo: HashMap<String, String>, callback: Callback<ResponNews>)

    @GET("/news/{id}")
    fun getNewsId(@Path("id") id: String, callback: Callback<ResponNewsId>)

    @PUT("/news/{id}/comment")
    fun putNewsComment(@Path("id") id: String, @Header("Authorization") a:String, @BodyMap map: HashMap<String,String>, callback: Callback<NewsModel>)

    @PUT("/news/activity/{id}/comment")
    fun putNewsActivityComment(@Path("id") id: String,@Header("Authorization") a:String, @BodyMap map: HashMap<String,String>, callback: Callback<GetActivities>)

    @PUT("/news/profile/notifid")
    fun putProfileNotifId(@Header("Authorization") a: String, @BodyMap map: HashMap<String, String>, callback: Callback<Profile>)

    @POST("/user")
    fun postParent(@Body filter: FilterModel, callback: Callback<ResponUsers>)

    @EndPoint("https://onesignal.com/api/v1")
    @Headers(
            "Content-Type: application/json; charset=utf-8"
    )
    @POST("/notifications")
    fun postNotifify(@Header("Authorization") a:String,@Body notif: Notify, callback: Callback<ResponNotif>)
}
