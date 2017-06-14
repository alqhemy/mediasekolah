package com.media.schoolday.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import com.ceylonlabs.imageviewpopup.ImagePopup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kosalgeek.android.photoutil.ImageLoader
import com.media.schoolday.R
import com.media.schoolday.SchoolApp
import com.media.schoolday.models.*
import com.media.schoolday.utility.DbLocal
import com.media.schoolday.utility.PfUtil
import com.orhanobut.wasp.Callback
import com.orhanobut.wasp.Response
import com.orhanobut.wasp.WaspError
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_news.*
import kotlinx.android.synthetic.main.fragment_news_post.*
import kotlinx.android.synthetic.main.fragment_news_post.view.*
import kotlinx.android.synthetic.main.imageview_item.view.*
import okhttp3.Call
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


class NewsFragment: Fragment(), AnkoLogger {

    lateinit private var linear: LinearLayout
    lateinit private var root: View
    lateinit var spinner: Spinner
    lateinit var spinnerTopic: Spinner
    lateinit private var imgPop:ImagePopup
    private val listPhoto:ArrayList<Pair<Int,String>> = ArrayList()
    private var index = 1
    private val listPicture = ArrayList<String>()
    private val TOPIC = "topik"
    private val CATEGORI = "kelas"
    interface OnItemSelectedListener {
        fun onPost(link: String, file: String)
        fun onUserClick(school: String)
    }
    var callback: OnItemSelectedListener? = null


    companion object {
        fun getInstance(s: String):NewsFragment {
            val fragment = HomeFragment()
            val args = Bundle()
            args.putString("title",s)
            fragment.arguments = args
            return NewsFragment()
        }

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        root =  inflater!!.inflate(R.layout.fragment_news_post,container,false)
        linear = root.findViewById(R.id.linearMain) as LinearLayout
        callback = activity as? OnItemSelectedListener
        getSpinClass(root)
        getSpinTopic(root)
        imagePopInitial()

        return root
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
    fun cacheData(){
        if( DbLocal.postNews() != null){
            val data =  DbLocal.postNews()
            root.tiNewsPostDescription.setText(data?.description)
            root.tiNewsPostTitle.setText(data?.title)
            getListClass(CATEGORI)?.forEachIndexed { index, s ->
                if(s == data?.topic)
                    root.spNewsPostTopic.setSelection(index)
            }
            getListClass(TOPIC)?.forEachIndexed { index, s ->
                if(s == data?.category)
                    root.spNewsPostKegitan.setSelection(index)
            }
        }
        if (DbLocal.postPhoto() != null){
            linear.removeAllViews()
            val pic = DbLocal.postPhoto()
            listPhoto.clear()
            listPicture.clear()
            index = 1
            pic?.gambar?.forEach {
                addPhoto( it )
                listPicture.add(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cacheData()

    }
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity.toolbarNews.title = "Posting Berita"
        activity.toolbarNews.setNavigationOnClickListener() {
            activity.onBackPressed()
        }
        spinner.setOnFocusChangeListener { _ , hasFocus ->
            if(hasFocus){
                context.toast("spiine change")
            }
        }
        ibNewsPostDelete.setOnClickListener { removePhoto()}
        ibNewsPostImage.setOnClickListener { takePhoto() }
        ibNewsPostChild.setOnClickListener { getListAnak() }
    }
    fun getSpinClass(v: View?){
        spinner = v!!.findViewById(R.id.spNewsPostTopic) as Spinner
        val adapter = ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, getListClass(CATEGORI))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }
    fun getSpinTopic(v: View?){
        spinnerTopic = v!!.findViewById(R.id.spNewsPostKegitan) as Spinner
        val adapter = ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, getListClass(TOPIC))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTopic.adapter = adapter
    }

    fun getListAnak(){
        postSave()
        if(spinner.selectedItem != "Umum" ) {
            val topic = spinner.selectedItem.toString()
            val kegiatan = spinnerTopic.selectedItem.toString()
            val ft = activity.supportFragmentManager.beginTransaction()
            with(ft) {
                setCustomAnimations(
                        R.anim.abc_fade_in, R.anim.abc_fade_out, R.anim.abc_popup_enter, R.anim.abc_popup_exit)
                replace(com.media.schoolday.R.id.news_fragment, ActitivityFragment.newInstance(topic,kegiatan))
                addToBackStack(null)
                commit()
            }
        }
    }

    fun getListClass(filter: String):ArrayList<String>?{
        val profile = DbLocal.getProfile()?.teacher?.first()
        val sekolah = DbLocal.schoolList().filter { it.nama == profile?.sekolah}

        return when(filter) {
            CATEGORI -> {
                sekolah.filter { it.nama == profile?.sekolah }.first().kelas
            }
            TOPIC -> {
                sekolah.filter { it.nama == profile?.sekolah }.first().topik
            }
            else -> {
                ArrayList()
            }
        }
    }


    private fun removePhoto() {
        linear.removeAllViews()
        listPhoto.clear()
        listPicture.clear()
        PfUtil.clear("post","photo")
        index = 1
    }

    fun takePhoto(){
        CropImage.activity(null)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(context, this@NewsFragment)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(resultCode){
            RESULT_OK -> {
                when(requestCode){
                    CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                        val result = CropImage.getActivityResult(data)
                        listPicture.add(result.uri.path)
                        addPhoto(result.uri.path)
                        postSave()
                    }
                }
            }
        }
    }

    fun postSave(){

        val post = PostNews()
        with(post){
            topic = spinner.selectedItem.toString()
            category = spinnerTopic.selectedItem.toString()
            title = root.tiNewsPostTitle.text.toString()
            description = root.tiNewsPostDescription.text.toString()
        }
        val gson = Gson()
        val type = object : TypeToken<PostNews>() {}.type
        val json = gson.toJson(post,type)
        PfUtil.saveJsonObject("post","news", JSONObject(json))

        val list  = listPicture.clone() as ArrayList<String>
        val photo = PostPhoto(list)
        val typePhoto = object : TypeToken<PostPhoto>() {}.type
        val jsonPhhoto = gson.toJson(photo,typePhoto)
        PfUtil.saveJsonObject("post","photo", JSONObject(jsonPhhoto))
    }

    fun addPhoto(photo: String){

        listPhoto.add(index to photo)
        index
        linear.layoutParams.height = 200
        try {
            val bitmap = ImageLoader.init().from(photo).requestSize(512, 512).getBitmap()
            val draw = ImageLoader.init().from(photo).requestSize(512, 512).imageDrawable
            val viewPhoto = LayoutInflater.from(activity.baseContext).inflate(R.layout.imageview_item, null)
            viewPhoto.image.setImageBitmap(bitmap)
            viewPhoto.image.setOnClickListener { imgPop.initiatePopup( draw ) }
            viewPhoto.tag = index
            linear.addView(viewPhoto)
            index++
        }catch (e: FileNotFoundException) {
            activity.toast("something wrong")
        }
    }

    override fun onDetach() {
        callback = null
        super.onDetach()
    }
    fun postNews(){
        postSave()
        val post = PostNews()
        val aktifitas = DbLocal.activities()
        post.title = root.tiNewsPostTitle.text.toString()
        post.description = root.tiNewsPostDescription.text.toString()
        post.category = spNewsPostKegitan.selectedItem.toString()
        post.topic = spNewsPostTopic.selectedItem.toString()

        if(aktifitas.isNotEmpty())  post.aktifitas.addAll(aktifitas)

        SchoolApp.rest?.postNews(DbLocal.token(),post, object: Callback<ResponPostNews>{
            override fun onError(error: WaspError?) {
                context.toast("Network failed, please try again later")
            }

            override fun onSuccess(response: Response?, t: ResponPostNews?) {
                postnewsNotify(t!!)
                if(listPicture.count() > 0) {
                    postPhotos(t!!.news[0].id)
                }
                else {
                    PfUtil.clear("post","news")
                    PfUtil.clear("post","activity")
                    callback?.onPost("stop","stop")
                }
            }
        })
    }
    fun postPhotos(id: String){

        val url = "http://128.199.199.232/web/api/news/$id/photo"
        listPicture.forEach {
            callback?.onPost("start",it)
            val okclient = OkHttpClient()
            val media = okhttp3.MediaType.parse("image/jpeg")
            val filePic = File(it)
            val request  = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("photo","photo.jpg",okhttp3.RequestBody.create(media,filePic)).build()
            val req = okhttp3.Request.Builder().url(url).put(request).build()
            okclient.newCall(req).enqueue(object: okhttp3.Callback{
                override fun onFailure(call: Call?, e: IOException?) {
                    callback?.onPost("stop",it)
                }
                override fun onResponse(call: Call?, response: okhttp3.Response?) {
                    callback?.onPost("stop",it)
                }
            })

        }
    }

    override fun onCreateOptionsMenu(menu:Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.fragment_menu,menu)
    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.item_save -> {
                if(root.tiNewsPostDescription.text.isNullOrBlank() || root.tiNewsPostTitle.text.isNullOrBlank()){
                    activity.alert("Judul dan isi berita tidak boleh kosong"){ yesButton {  }}.show()
                }
                else{

                    postNews()
                }
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    fun postnewsNotify(post: ResponPostNews) {
        val data = post.parent.filterNot { it.playerid.isNullOrEmpty() }
                .map { it.playerid } as ArrayList<String>
        if(data.isNotEmpty()){
            val notif = Notify(
                    "a18a2159-9184-4fd3-9e82-f91b0eebd2db",
                    data,
                    Content(post.news[0].category.toString() + " " + post.news[0].topic.toString()),
                    Content(post.news[0].title.toString())
            )
            sendNotif(notif)
        }

    }

    fun sendNotif(notif: Notify){
        val auth = "Basic MmY3NTE4NjItY2Q3NC00NTkxLWFmZGItYmQ4MmMwNGZmODM0"
        SchoolApp.rest?.postNotifify(auth,notif, object : Callback<ResponNotif>{
            override fun onSuccess(response: Response?, t: ResponNotif?) {

            }
            override fun onError(error: WaspError?) {

            }
        })

    }




}

