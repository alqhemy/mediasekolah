package com.media.schoolday.api

/*
 * Created by yosi on 19/05/2017.
 */

import com.media.schoolday.models.model.*
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

    @PUT("/profile/anak")
    fun putProfileAnak(@Header("Authorization") a: String, @Body child: ProfilAnak, callback: Callback<Profile>)

    @PUT("/profile/guru")
    fun putProfileGuru(@Header("Authorization") a: String, @Body guru: ProfileGuru, callback: Callback<Profile>)

    @POST("/news")
    fun postNews(@Header("Authorization") news: PostNews, callback: Callback<NewsModel>)





}