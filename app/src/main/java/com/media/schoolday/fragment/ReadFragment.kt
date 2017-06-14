package com.media.schoolday.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.ceylonlabs.imageviewpopup.ImagePopup
import com.media.schoolday.R
import com.media.schoolday.SchoolApp
import com.media.schoolday.models.*
import com.media.schoolday.utility.BitmapScaler
import com.media.schoolday.utility.DbLocal
import com.media.schoolday.utility.loadWithGlade
import com.orhanobut.wasp.Callback
import com.orhanobut.wasp.Response
import com.orhanobut.wasp.WaspError
import kotlinx.android.synthetic.main.activity_news.*
import kotlinx.android.synthetic.main.fragment_find.view.*
import kotlinx.android.synthetic.main.imageview_item.view.*
import kotlinx.android.synthetic.main.list_item_find.view.*
import kotlinx.android.synthetic.main.list_news_home.view.*
import org.jetbrains.anko.*
import org.json.JSONObject
import java.text.SimpleDateFormat

/*
 * Created by yosi on 10/05/2017.
 */

class ReadFragment: Fragment(),AnkoLogger{

    val KEY_ID = "id"
    var find = ""
    lateinit var imgPop: ImagePopup
    lateinit var adapter:ArrayAdapter<Comment>
    lateinit var glidApp: Glide
    lateinit var root: View
    interface ReadNewsListener {
        fun onMessage(message: String) {}
    }
    var callback: ReadNewsListener? = null



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
        callback = activity as? ReadNewsListener
        imagePopInitial()
        glidApp = Glide.get(context)
    }

    fun imagePopInitial(){

        val width = resources.displayMetrics.widthPixels
        val heigh = resources.displayMetrics.heightPixels
        imgPop = ImagePopup(context)
        imgPop.apply {
            setBackgroundColor(Color.BLACK)
            setWindowWidth(width)
            setWindowHeight(heigh)
            setHideCloseIcon(true)
            setImageOnClickClose(true)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        root = inflater!!.inflate(R.layout.fragment_find,container,false)
        val news = DbLocal.newsList().filter { it.id == find }.first()
        if(DbLocal.getPostId()?.news?.id  != null && DbLocal.getPostId()?.news?.id == find)
            createData(DbLocal.getPostId()!!)
        else
            getNewsId(find!!)

        with(root){
            tvHomeItemSekolah.text = news.sekolah
            tvHomeItemUser.text = news.userName
            tvHomeItemDate.text = SimpleDateFormat("dd-MM-yyyy HH:mm").format(news.timeCreated).toString()
            tvHomeItemTitle.text = news.title
            tvHomeItemDescription.text = news.description
            tvHomeItemUserTitle.text = news.userTitle

            if(news.topic == "Umum") {
                tvHomeItemTopic.text = news.category + " " + news.topic
                activity.toolbarNews.title = news.category + " " + news.topic
            }
            else {
                tvHomeItemTopic.text = news.category + " Kelas " + news.topic
                activity.toolbarNews.title = news.category + " Kelas " + news.topic
            }
        }
        if (news.comments != null)
            createComent(news.comments)

        if (news.photo != null)
            createPhoto(news.photo)
        activity.toolbarNews.setNavigationOnClickListener() {
            activity.onBackPressed()
        }

        return root

    }
    fun createComent(comment: ArrayList<Comment>){

        val komentar = TextView(context)
        komentar.setText("Komentar...")
//        val card = CardView(context)
//        card.addView(komentar)
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
    fun createPhoto(photo: ArrayList<Photo>){
        val url = "http://128.199.199.232/web/api/news/photo/"
        photo.forEach {
            val viewPhoto = LayoutInflater.from(context).inflate(R.layout.imageview_item, null)
            viewPhoto.image.loadWithGlade(url + it.filename )
            viewPhoto.image.isDrawingCacheEnabled = true
            viewPhoto.image.setOnClickListener{
                val bit = BitmapScaler.scaleToFitWidth(viewPhoto.image.drawingCache,activity.resources.displayMetrics.widthPixels)
                val draw: Drawable = BitmapDrawable(context.resources,bit)
                imgPop.initiatePopup( draw )
            }
            root.linearReadPicture.addView(viewPhoto)
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
        SchoolApp.rest?.putNewsComment(find,DbLocal.token(),comment,object: Callback<NewsModel>{
            override fun onSuccess(response: Response?, t: NewsModel?) {
                root.linearReadComment?.removeAllViews()
                createComent(t!!.comments)
                callback?.onMessage("update news")
                root.scrolNewsMain.post {
                    run {
                        root.scrolNewsMain.fullScroll(ScrollView.FOCUS_DOWN)
                    }
                }
            }

            override fun onError(error: WaspError?) {
                context.toast("Network failed, please try again later..")
            }
        })
    }
    fun createData(t: ResponNewsId){

        if(t?.aktifitas != null){
            if(t.news.userId == DbLocal.getProfile()?.user?.email){
                addAktifitas(t.aktifitas)
            }
            else {
                val filter = DbLocal.getProfile()?.child?.map { it.nis }  as ArrayList<String>
                addAktifitas( t.aktifitas.filter { it.nis in filter } as ArrayList<GetActivities> )
            }

        }
    }

    fun addAktifitas(list: ArrayList<GetActivities>){
        root.linearReadAktifitas.removeAllViews()
//        val siswa = DbLocal.getProfile()?.child?.map { it.nama } as ArrayList<String>
        list.forEachIndexed { index, getActivities ->
            val tvAnak = TextView(context)
            val tvComment = TextView(context)
            val card = CardView(context)
            val layout = LinearLayout(context)
            val layout2 = LinearLayout(context)
            val image = ImageView(context)
            layout.orientation = LinearLayout.HORIZONTAL
            layout2.orientation = LinearLayout.VERTICAL
            card.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,120)

            image.image = activity.resources.getDrawable(R.drawable.ic_library_books_black_24dp)
            image.setPadding(20,10,20,5)
            with(tvAnak){
                text = list[index].name
                setPadding(20,10,0,5)
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT, 1F)
//                textColor = resources.getColor(R.color.colorPrimaryDark)
                setTypeface(Typeface.DEFAULT_BOLD)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)

            }
            with(tvComment){
                text = "Komentar :" + list[index].comments.count().toString()
                setPadding(20,0,0,0)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12F)
                textColor = resources.getColor(R.color.colorText38)

            }
            layout.setOnClickListener {
                val id = list[index].nis
                val ft = activity.supportFragmentManager.beginTransaction()
                with(ft) {
                    setCustomAnimations(
                            R.anim.abc_fade_in, R.anim.abc_fade_out, R.anim.abc_popup_enter, R.anim.abc_popup_exit)
                    replace(com.media.schoolday.R.id.news_fragment, ReadDetailFragment.newInstance(find!!,id!!))
                    addToBackStack(null)
                    commit()
                }
            }

            layout.addView(tvAnak)
            layout.addView(image)
            layout2.addView(layout)
            layout2.addView(tvComment)
            card.addView(layout2)
            root.linearReadAktifitas.addView(card)
        }

    }

    fun getNewsId(id: String){
        SchoolApp.rest?.getNewsId(id, object : Callback<ResponNewsId>{
            override fun onSuccess(response: Response?, t: ResponNewsId?) {
                val userRef = JSONObject(response!!.body)
                SchoolApp.prefs!!.saveJSONObject("news", "newsId", userRef)
                if(t != null)
                    createData(t)
            }

            override fun onError(error: WaspError?) {
            }
        })
    }

    override fun onDetach() {
        callback = null
        super.onDetach()
    }


}


