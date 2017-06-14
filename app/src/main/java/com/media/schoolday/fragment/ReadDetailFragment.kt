package com.media.schoolday.fragment

/*
 * Created by yosi on 30/05/2017.
 */

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.media.schoolday.R
import com.media.schoolday.SchoolApp
import com.media.schoolday.models.Aktifitas
import com.media.schoolday.models.Comment
import com.media.schoolday.models.GetActivities
import com.media.schoolday.models.ResponNewsId
import com.media.schoolday.utility.DbLocal
import com.orhanobut.wasp.Callback
import com.orhanobut.wasp.Response
import com.orhanobut.wasp.WaspError
import kotlinx.android.synthetic.main.activity_news.*
import kotlinx.android.synthetic.main.fragment_find.view.*
import kotlinx.android.synthetic.main.list_item_find.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.padding
import org.jetbrains.anko.textColor
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat

/*
 * Created by yosi on 10/05/2017.
 */

class ReadDetailFragment: Fragment(),AnkoLogger{

    val KEY_ID = "id"
    var find  = ""
    var key = ""
    lateinit var root : View
    lateinit var adapter : ArrayAdapter<Comment>
    interface OnClicItemListener {
        fun onActivityMessage(id: String) {}
    }
    var callback: OnClicItemListener? = null
    var title = ""


    companion object {
        fun newInstance(s: String, id: String): ReadDetailFragment {
            val fragment = newInstance()
            val args = Bundle()
            args.putString("title", s)
            args.putString("id",id)
            fragment.arguments = args
            return fragment
        }
        fun newInstance(): ReadDetailFragment {
            return ReadDetailFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(arguments != null && arguments.containsKey(KEY_ID)){
            find = arguments.getString(KEY_ID)
            key = arguments.getString("title")
        }
        callback = activity as? OnClicItemListener
        setHasOptionsMenu(true)
    }



    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        root = inflater!!.inflate(R.layout.fragment_find,container,false)
        val aktivitas = DbLocal.getPostId()?.aktifitas?.filter { it.nis == find }?.first()
        if (aktivitas?.aktifitas != null ){
            key = aktivitas.id ?: "0"
            createView(root, aktivitas.aktifitas)
            if(aktivitas.comments != null)
                 createComment(aktivitas.comments)
        }
        activity.toolbarNews.setNavigationOnClickListener() {
            val news =  DbLocal.getPostId()?.news
            activity.toolbarNews.title = "Aktifitas Siswa"
            if(news?.topic == "Umum") {
                activity.toolbarNews.title = news.category + " " + news.topic
            }
            else {
                activity.toolbarNews.title = news?.category + " Kelas " + news?.topic
            }
                activity.onBackPressed()
        }
//        else
//            getNewsId(key,root)

        root.linearNewsDetil.removeView(root.include2)
        return root

    }

    fun createView(v : View, list: ArrayList<Aktifitas>){
        activity.toolbarNews.title = "Aktifitas Siswa"
        list.forEachIndexed { index, getActivities ->
            val tvTitle = TextView(context)
            val tvDescription = TextView(context)
            val card = CardView(context)
            val linear = LinearLayout(context)
            linear.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            linear.orientation = LinearLayout.VERTICAL
            card.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            with(tvTitle){
                text = list[index].title
                padding = 20
                setPadding(20,10,0,5)
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                textColor = resources.getColor(R.color.colorText38)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12F)
            }
            with(tvDescription){
                text = list[index].description
                setPadding(20,0,20,10)
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
            }
            linear.addView(tvTitle)
            linear.addView(tvDescription)
            card.addView(linear)
            v.linearReadAktifitas.addView(card)
        }
    }

    fun createComment(comment: ArrayList<Comment>){
        if(comment.size > 0) {
            val komentar = TextView(context)
            komentar.setText("Komentar...")
            komentar.padding = 10
            root.linearReadComment.addView(komentar)

            comment.forEach {
                val view2 = LayoutInflater.from(activity.baseContext).inflate(R.layout.list_item_find, null)
                view2.tvFindItemUser.text = it.user
                view2.tvFindItemDescription.text = it.comment
                view2.tvFindItemDate.text = SimpleDateFormat("dd-MM-yyyy HH:mm").format(it.timeCreated).toString()
                view2.tvFindItemUserTitle.text = it.title
                root.linearReadComment?.addView(view2)
            }
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        view?.ibNewsComment?.setOnClickListener {
            sendComment(view.editNewsComment.text.toString())
            view.editNewsComment.setText("")
        }
    }

    fun sendComment(msg: String){
        val comment = HashMap<String,String>()
        comment.put("comment",msg)
        SchoolApp.rest?.putNewsActivityComment(key,DbLocal.token(),comment,object: Callback<GetActivities>{
            override fun onSuccess(response: Response?, t: GetActivities?) {
                root.linearReadComment?.removeAllViews()
                createComment(t!!.comments)
                callback?.onActivityMessage(t.newsId.toString())
                root.scrolNewsMain.post {
                    run {
                        root.scrolNewsMain.fullScroll(ScrollView.FOCUS_DOWN)
                    }
                }
                getNewsId(t.newsId!!)
            }

            override fun onError(error: WaspError?) {
                context.toast("Network failed, please try again later..")
            }
        })
    }
    fun getNewsId(id: String){
        SchoolApp.rest?.getNewsId(id, object : Callback<ResponNewsId> {
            override fun onSuccess(response: Response?, t: ResponNewsId?) {

            }

            override fun onError(error: WaspError?) {
                context.toast("Network failed")
            }
        })
    }
    override fun onDetach() {
        callback = null
        super.onDetach()
    }

}