package com.media.schoolday.models.model

import android.app.Activity
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by yosi on 03/05/2017.
 */

class ResponNews(@SerializedName("news") val data: ArrayList<NewsModel>)

data class NewsModel(
    @SerializedName("_id")
    val id: String,
    val sekolah: String,
    val title: String,
    val topic: String,
    val description: String,
    val category: String,
    val publish: String,
    val userId: String,
    val userName: String,
    val userTitle: String,
    val timeCreated: Date,
    val comments: ArrayList<Comment>?,
    val photo: ArrayList<Photo>?
)

data class Comment(val comment: String, val user: String, val title: String, val timeCreated: Date )

data class Photo(val location: String, val filename: String)

data class PostNews(
        val title: String,
        val description: String,
        val topic: String,
        val category: String,
        val publish: Boolean)

class PostActivities (){
    @SerializedName("_id")
    var id: String? = null
    var nis: String? = null
    var name: String? = null
    var news: String? = null
    var activity: ArrayList<Activity>? = null
}
class Activity{
    var title: String? = null
    var description: String? = null
}