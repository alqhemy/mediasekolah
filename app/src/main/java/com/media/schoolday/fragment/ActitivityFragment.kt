package com.media.schoolday.fragment

import android.content.Context
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.media.schoolday.R
import com.media.schoolday.models.Aktifitas
import com.media.schoolday.models.PostActivities
import com.media.schoolday.models.PostActivity
import com.media.schoolday.utility.DbLocal
import com.media.schoolday.utility.PfUtil
import kotlinx.android.synthetic.main.activity_news.*
import kotlinx.android.synthetic.main.fragment_news_activity.view.*
import kotlinx.android.synthetic.main.input_item.view.*
import org.jetbrains.anko.AnkoLogger
import org.json.JSONObject
import java.util.*


/*
 * Created by yosi on 21/05/2017.
 */

class ActitivityFragment: Fragment(),AnkoLogger{

    private val listPost = ArrayList<PostActivities>()
    interface OnItemClickListener {
        fun sendMessage(arg: String)
    }
    lateinit var views: View
    lateinit var root: View
    lateinit var adapter: ArrayAdapter<PostActivities>
    var nomorID = ""
    var kegitan = ""
    var idKey: String = ""
    var status = false
    var callback: OnItemClickListener? = null

    companion object{

        fun newInstance(idNews: String,kegiatan: String): ActitivityFragment{
            val fragment = ActitivityFragment()
            val bundle = Bundle()
            fragment.idKey = idNews
            fragment.kegitan = kegiatan
            bundle.putString("id", idNews )
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        callback = activity as? OnItemClickListener
//        context.toast(idKey.toString())
        root = inflater!!.inflate(R.layout.fragment_news_activity,container,false)
        adapterUser()
        root.etNewsActivityAnak.visibility = View.GONE
        val toolbar = activity.findViewById(R.id.toolbarNews) as Toolbar
        (activity as AppCompatActivity).setSupportActionBar( toolbar )
        toolbar.title = idKey

        return root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity.toolbarNews.title = "Kelas $idKey"
        activity.toolbarNews.setNavigationOnClickListener() {
            if (status)
                editChilde()
            else{
                activity.toolbarNews.title = "Posting Berita"
                activity.onBackPressed()
            }
        }
        hideKeyboard()

    }

    fun adapterUser(){
        adapter = object : ArrayAdapter<PostActivities>(context,android.R.layout.simple_list_item_1,listPost){
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val item = getItem(position)
                var vi = convertView
                if (vi == null){
                    vi = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
                }
                val name = vi!!.findViewById(android.R.id.text1) as TextView
//                val nis = vi.findViewById(android.R.id.text2) as TextView
                name.text = item.name
//                nis.text = item.nis
                vi.setOnClickListener {
                    view?.etNewsActivityAnak?.setText(item.name)
                    nomorID = item.name!!
                    status = true
                    root.listNewsActivityUser.visibility = View.GONE
                    root.etNewsActivityAnak.visibility = View.VISIBLE
                    postEdit(item)
                }
                return vi
            }
        }
        adapter.setNotifyOnChange(true)
        root.listNewsActivityUser.adapter = adapter
        DbLocal.activities().forEach { callback?.sendMessage(it.toString()) }
        if(DbLocal.activities().filter { it.kelas == idKey && it.kegiatan == kegitan }.isEmpty() ) {

            listPost.addAll(createListActivity())
            adapter.notifyDataSetChanged()
        }
        else{
            listPost.addAll(DbLocal.activities())
            adapter.notifyDataSetChanged()
        }
    }

    fun removeView(){
        root.linearActivityInput.removeAllViews()
        root.etNewsActivityAnak.setText("")
    }

    fun postEdit(post: PostActivities){

        post.aktifitas.forEach {
            views = LayoutInflater.from(activity.baseContext).inflate(R.layout.input_item, null)
            val edit = TextInputEditText(context)
            with(edit){
                hint = it.title
                id = View.generateViewId()
                tag = it.title
                setText(it.description)
            }

            views.inputLayout.addView(edit)
            root.linearActivityInput.addView(views)
        }
    }

    fun postSave(){
        if(listPost.isNotEmpty()) {
            val post = listPost.filter { it.name == nomorID }.first()
            post.aktifitas.forEach {
                val edit = root.findViewWithTag(it.title) as TextInputEditText
                it.description = edit.text.toString()
            }
            adapter.notifyDataSetChanged()
        }
    }

    fun createListActivity(): ArrayList<PostActivities>{

        callback?.sendMessage("kelas $idKey")
        val post = ArrayList<PostActivities>()

        val listEntryPost = DbLocal.studentList().filter { it.kelas == idKey }
        listEntryPost.forEach {
            callback?.sendMessage(it.nis+ " "+it.nama)
            val postActivity = PostActivities()
            val listEntryActivity = ArrayList<Aktifitas>()

            listActivity().forEach {
                val aktifitas = Aktifitas()
                aktifitas.title = it
                aktifitas.description = ""
                listEntryActivity.add(aktifitas)
            }
            postActivity.apply {
                callback?.sendMessage(it.nis+ " "+it.nama)
                nis = it.nis
                name = it.nama
                school = it.sekolah
                kelas = it.kelas
                kegiatan = kegitan
                aktifitas.addAll(listEntryActivity)

            }
            post.add(postActivity)
        }
        return post
    }

    fun listActivity():ArrayList<String>{
        val profile = DbLocal.getProfile()
        val sekolah = DbLocal.schoolList()
        if(sekolah != null) {
            val filter = sekolah.filter { it.nama == profile!!.teacher.first().sekolah }
            val kegitan = filter.first().activity?.
                    filter { it.judul == kegitan && it.status == true }?.
                    map{ it.deskripsi} as ArrayList<String>
            return kegitan
        }
        else {
            return ArrayList<String>()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.fragment_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.item_save -> {
                if(status){
                    postSave()
                    editChilde()
                }

            }
            else -> super.onOptionsItemSelected(item)
        }
//        activity.supportFragmentManager.popBackStack()
        return true
    }

    fun hideKeyboard(){
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
    fun cacheData(){
        val actitivity = PostActivity(listPost)
        val gson = Gson()
        val type = object : TypeToken<PostActivity>() {}.type
        val json = gson.toJson(actitivity,type)
        callback?.sendMessage(json)
        PfUtil.saveJsonObject("post","activity",JSONObject(json))
    }

    fun editChilde(){
        if (root.linearActivityInput.childCount > 0 ) {
            hideKeyboard()
            removeView()
            cacheData()
        }
        root.etNewsActivityAnak.visibility = View.GONE
        root.listNewsActivityUser.visibility = View.VISIBLE
        status = false


    }
    override fun onDetach() {
        callback = null
        super.onDetach()
    }

}
