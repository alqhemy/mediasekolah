package com.media.schoolday.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import com.media.schoolday.R
import com.media.schoolday.SchoolApp
import com.media.schoolday.fragment.ActitivityFragment
import com.media.schoolday.fragment.NewsFragment
import com.media.schoolday.fragment.ReadDetailFragment
import com.media.schoolday.fragment.ReadFragment
import com.media.schoolday.models.ResponNews
import com.media.schoolday.utility.DbLocal
import com.media.schoolday.utility.PfUtil
import com.orhanobut.wasp.Callback
import com.orhanobut.wasp.Response
import com.orhanobut.wasp.WaspError
import kotlinx.android.synthetic.main.content_news.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.ctx
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import org.json.JSONObject

class NewsActivity : AppCompatActivity(), AnkoLogger,
        NewsFragment.OnItemSelectedListener,
        ReadFragment.ReadNewsListener,ReadDetailFragment.OnClicItemListener,
        ActitivityFragment.OnItemClickListener
{
    lateinit var title:String
    lateinit var id: String
    var responStatus = false
    var que = ArrayList<String>()
    lateinit var progres: ProgressDialog

    interface OnItemUpdateListener {
        fun OnProfileUpdate(args: String)
    }
    var callback: OnItemUpdateListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progres = ProgressDialog(this@NewsActivity)
        setContentView(R.layout.activity_news)
        val toolbar = findViewById(R.id.toolbarNews) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        title = intent.getStringExtra("post")
        id = intent.getStringExtra("id")

        if (savedInstanceState == null) {
            getFragment(title)
        }

        with(progres){
            setTitle("Upload data...")
            setMessage("Please wait...")
            setCancelable(false)
        }
        callback = ctx as? OnItemUpdateListener
    }

    fun getFragment(title: String) {
        when(title){
            "new" -> {
                supportActionBar!!.title = "New Post"
                changeFragment(NewsFragment.getInstance("News Post"),"newsPost",true)
            }
            "read" -> {
                supportActionBar!!.title = "News detail"
                changeFragment(ReadFragment.newInstance("News read",id),"readPost")
            }

        }
    }
    fun changeFragment(f: Fragment, tag: String, cleanStack: Boolean = false) {
        val ft = supportFragmentManager.beginTransaction()
        if (cleanStack) {
            clearBackStack()
        }
        with(ft){
            setCustomAnimations(
                    R.anim.slide_in_right, R.anim.slide_out_left,
                    R.anim.abc_popup_enter, R.anim.abc_popup_exit)
            replace(R.id.news_fragment, f, tag)
            addToBackStack(null)
            commit()
        }

    }

    fun clearBackStack() {
        val manager = supportFragmentManager
        if (manager.backStackEntryCount > 0) {
            val first = manager.getBackStackEntryAt(0)
            manager.popBackStack(first.id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 1) {
            fragmentManager.popBackStack()
        } else {
            val data = Intent()
            data.putExtra("title",title)
            setResult(Activity.RESULT_OK,data)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onPost(status: String, file: String) {

        progres.show()
        if(status == "start")
            que.add(file)
        else{
            que.remove(file)
            if( que.count() == 0) {
                progres.dismiss()
                PfUtil.clear("post","photo")
                PfUtil.clear("post","activity")
                PfUtil.clear("post","news")
                PfUtil.clear("post","picture")
                val data = Intent()
                data.putExtra("title",title)
                setResult(Activity.RESULT_OK,data)
                finish()

            }
        }
    }

    override fun onUserClick(school: String) {
        msgNotif(school)
    }

    override fun sendMessage(arg: String) {
        info { arg }
    }

    fun msgNotif(msg: String){
        Snackbar.make(news_fragment, msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()

    }

    override fun onMessage(message: String) {
        info { message }
        hideKeyboard()
        getNews()
    }
    fun getNews(){

        SchoolApp.rest?.getNews(DbLocal.token(), object : Callback<ResponNews> {
            override fun onError(error: WaspError?) {
                toast("Network Failed")
            }
            override fun onSuccess(response: Response?, t: ResponNews?) {
                val data = JSONObject(response?.body)
                SchoolApp.prefs?.saveJSONObject("school", "news", data)
            }
        })
    }

    fun hideKeyboard(){
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(news_fragment.windowToken, 0)
    }

    override fun onActivityMessage(id: String) {
        val fr = supportFragmentManager.findFragmentByTag("readPost") as ReadFragment
        fr.getNewsId(id)
    }


}
