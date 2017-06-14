package com.media.schoolday.utility

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.media.schoolday.models.*
import org.json.JSONArray

/*
 * Created by yosi on 10/05/2017.
 */

object DbLocal{
    val gson = Gson()
    var text = ""
    fun token():String{
        val userid = PfUtil.getJsonObject("user","user")
        if(userid.isNull("token")){
            return ""
        }else {
            val tokenUser = userid.get("token").toString()
            return tokenUser
        }
    }
    fun getUser():User?{
        val data = PfUtil.getJsonObject("user","user")
        if( data.isNull("user")){
            return  null
        }
        else {
            val type = object : TypeToken<User>() {}.type
            val userPorfile: User = gson.fromJson(data.toString(),type)
            return userPorfile
        }
    }
    fun getProfile(): ResponProfile? {
        val data = PfUtil.getJsonObject("user","profile")
        if( data.isNull("user")){
            return  null
        }
        else {
            val type = object : TypeToken<ResponProfile>() {}.type
            val userPorfile: ResponProfile = gson.fromJson(data.toString(),type)
            return userPorfile
        }
    }
    fun schoolList():ArrayList<SchoolModel>{
        val data = PfUtil.getJsonObject("school","school")
        if(data.length() > 0 ) {
            val sekolah = data.get("data") as JSONArray
            val type = object : TypeToken<ArrayList<SchoolModel>>() {}.type
            val school: ArrayList<SchoolModel> = gson.fromJson(sekolah.toString(),type)
            return school
        }
        else{
            return ArrayList<SchoolModel>()
        }
    }
    fun studentList():ArrayList<Student> {
        val data = PfUtil.getJsonObject("school", "siswa")
        if(data.isNull("data")) return ArrayList<Student>()
        else{
            val student = data.getJSONArray("data")
            val type = object : TypeToken<ArrayList<Student>>() {}.type
            val result: ArrayList<Student> = gson.fromJson(student.toString(), type)
            return result
        }
    }
    fun teacherList():ArrayList<Teacher>{
        val data = PfUtil.getJsonObject("school","guru")
        if(data.length() > 0){
            val teacher = data.getJSONArray("data")
            val type = object : TypeToken<ArrayList<Teacher>>() {}.type
            val result: ArrayList<Teacher> = gson.fromJson(teacher.toString(), type)
            return result
        }
        else{
            return ArrayList<Teacher>()
        }
    }
    fun newsList():ArrayList<NewsModel>{
        val data = PfUtil.getJsonObject("school","news")
        if(data.isNull("news")) {
            return ArrayList<NewsModel>()
        }
        else{
            val news = data.getJSONArray("news")
            val type = object : TypeToken<ArrayList<NewsModel>>() {}.type
            val result: ArrayList<NewsModel> = gson.fromJson(news.toString(), type)
            return result
        }
    }

    fun activities(): ArrayList<PostActivities>{
        val data = PfUtil.getJsonObject("post","activity")
        if(data.isNull("activities")) {
            return ArrayList<PostActivities>()
        }
        else{
            val news = data.getJSONArray("activities")
            val type = object : TypeToken<ArrayList<PostActivities>>() {}.type
            val result: ArrayList<PostActivities> = gson.fromJson(news.toString(), type)
            return result
        }
    }
    fun postNews(): PostNews?{
        val data = PfUtil.getJsonObject("post","news")
        if(data.isNull("title")) {
            return null
        }
        else{
            val type = object : TypeToken<PostNews>() {}.type
            val result: PostNews = gson.fromJson(data.toString(), type)
            return result
        }
    }

    fun postPhoto():PostPhoto?{
        val data = PfUtil.getJsonObject("post","photo")
        if(data.isNull("gambar")) {
            return null
        }
        else{
            val type = object : TypeToken<PostPhoto>() {}.type
            val result: PostPhoto = gson.fromJson(data.toString(), type)
            return result
        }
    }

    fun getPostId():ResponNewsId?{
        val data = PfUtil.getJsonObject("news","newsId")
        if(data.isNull("aktifitas")) {
            return null
        }
        else{
            val type = object : TypeToken<ResponNewsId>() {}.type
            val result: ResponNewsId = gson.fromJson(data.toString(), type)
            return result
        }
    }

    fun getParent():ResponUsers?{
        val data = PfUtil.getJsonObject("user","parent")
        if(data.isNull("data")) {
            return null
        }
        else{
            val type = object : TypeToken<ResponUsers>() {}.type
            val result: ResponUsers = gson.fromJson(data.toString(), type)
            return result
        }
    }
}