package com.media.schoolday.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.media.schoolday.R
import com.media.schoolday.models.model.Comment
import com.media.schoolday.utility.DbLocal
import com.media.schoolday.utility.loadImg
import kotlinx.android.synthetic.main.fragment_find.view.*
import kotlinx.android.synthetic.main.imageview_item.view.*
import kotlinx.android.synthetic.main.list_item_find.view.*
import kotlinx.android.synthetic.main.list_news_home.view.*
import org.jetbrains.anko.AnkoLogger
import java.text.SimpleDateFormat
import java.util.*

/*
 * Created by yosi on 10/05/2017.
 */

class ReadFragment: Fragment(),AnkoLogger{

    val KEY_ID = "id"
    var find: String? = null
    lateinit var adapter:ArrayAdapter<Comment>
    interface FindListener {
        fun AlertMesage(s: String)
        fun onProcess(data: HashMap<String,String>) {}
    }
    var callback: FindListener? = null

    companion object {
        fun newInstance(s: String, id: String): ReadFragment {
            val fragment = newInstance()
            val args = Bundle()
            args.putString("title", s)
            args.putString("id",id)
            fragment.arguments = args
            return fragment
        }
        fun newInstance(): ReadFragment {
            return ReadFragment()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(arguments != null && arguments.containsKey(KEY_ID)){
            find = arguments.getString(KEY_ID)
        }
        callback = activity as? FindListener
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.fragment_find,container,false)
        val news = DbLocal.newsList().filter { it.id == find }.first()
        with(view){
            tvHomeItemUser.text = news.userName
            tvHomeItemDate.text = SimpleDateFormat("dd-MM-yyyy HH:mm").format(news.timeCreated).toString()
            tvHomeItemTitle.text = news.title
            tvHomeItemDescription.text = news.description
            tvHomeItemTopic.text = news.topic
            tvHomeItemUserTitle.text = news.userTitle
        }

        if (news.comments != null) {
//            activity.longToast("commentar ada")
            news.comments.forEach {
                val view2 = LayoutInflater.from(activity.baseContext).inflate(R.layout.list_item_find, null)
                view2.tvFindItemUser.text = it.user
                view2.tvFindItemDescription.text = it.comment
                view2.tvFindItemDate.text = SimpleDateFormat("dd-MM-yyyy HH:mm").format(it.timeCreated).toString()
                view2.tvFindItemUserTitle.text = it.title
                view.linearReadComment.addView(view2)
            }
        }
        val url = "http://128.199.199.232/web/api/news/photo/"
        if (news.photo != null) {
            news.photo.forEach {
                val viewPhoto = LayoutInflater.from(activity.baseContext).inflate(R.layout.imageview_item, null)

                viewPhoto.image.setOnClickListener{
                }
                viewPhoto.image.loadImg(url + it.filename)
                view.linearReadPicture.addView(viewPhoto)
            }
        }
        return view

    }


}


