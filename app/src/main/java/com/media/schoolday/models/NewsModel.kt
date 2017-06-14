package com.media.schoolday.models

import com.google.gson.annotations.SerializedName
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by yosi on 03/05/2017.
 */

class ResponNews(@SerializedName("news") val data: ArrayList<NewsModel>)

class ResponPostNews(val news: ArrayList<NewsModel>,val parent: ArrayList<Profile>)

class ResponNewsId(val news: NewsModel, val aktifitas: ArrayList<GetActivities>)
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
        val comments: ArrayList<Comment>,
        val photo: ArrayList<Photo>?
)
data class Notify(
        val app_id: String,
        val include_player_ids: ArrayList<String>,
        val headings: Content,
        val contents: Content
)
class Content(val en: String)
class ResponNotif(val id: String, val recipients: Int)
data class Comment(val comment: String, val user: String, val title: String, val timeCreated: Date )
data class Photo(val location: String, val filename: String)

data class PostActivity(val activities: ArrayList<PostActivities> )

class PostNews{
    var title: String? = null
    var description: String? = null
    var topic: String? = null
    var category: String? = null
    var publish: Boolean = true
    var aktifitas: ArrayList<PostActivities> = ArrayList<PostActivities>()
}


class GetActivities (){
    @SerializedName("_id")
    var id: String? = null
    var nis: String? = null
    var newsId: String? = null
    var name: String? = null
    var school: String? = null
    var kelas: String? = null
    var kegiatan: String? = null
    var timeCreated: Date? = null
    var aktifitas: ArrayList<Aktifitas> = ArrayList<Aktifitas>()
    var comments: ArrayList<Comment> = ArrayList<Comment>()
}

class PostActivities (){
    var nis: String? = null
    var name: String? = null
    var school: String? = null
    var kelas: String? = null
    var kegiatan: String? = null
    var aktifitas: ArrayList<Aktifitas> = ArrayList<Aktifitas>()
}
class Aktifitas{
    var title: String? = ""
    var description: String = ""
}

class PostPhoto(val gambar: ArrayList<String>)