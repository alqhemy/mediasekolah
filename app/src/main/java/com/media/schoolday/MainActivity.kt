package com.media.schoolday

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ResultCodes
import com.google.firebase.auth.FirebaseUser
import com.media.schoolday.activity.Account
import com.media.schoolday.activity.NewsActivity
import com.media.schoolday.adapter.TabPageAdapter
import com.media.schoolday.fragment.HomeFragment
import com.media.schoolday.fragment.ReadFragment
import com.media.schoolday.models.*
import com.media.schoolday.utility.DbLocal
import com.media.schoolday.utility.PfUtil
import com.onesignal.OneSignal
import com.orhanobut.wasp.Callback
import com.orhanobut.wasp.Response
import com.orhanobut.wasp.WaspError
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast
import org.json.JSONObject

class MainActivity : AppCompatActivity(),AnkoLogger,
        ReadFragment.ReadNewsListener,
        NavigationView.OnNavigationItemSelectedListener {
    private val  RC_SIGN_IN = 123
    private val  RC_PROFILE = 112
    private val  RC_POST = 111
    lateinit var fab:FloatingActionButton
    lateinit var pageAdapter: TabPageAdapter
    lateinit var toolbar: Toolbar
    val pageTitle = arrayOf("Berita","Aktivitas")

    interface OnItemClickListener {
        fun OnDataUpdate(args: ArrayList<NewsModel>)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        fab = findViewById(R.id.fab) as FloatingActionButton
        fab.hide()
        fab.setOnClickListener {
            startActivityForResult<NewsActivity>(RC_POST, "post" to "new","id" to "0")
        }
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)
        createTab()
        checkAccount()
    }
    fun snackBar(msg: String){
        Snackbar.make(view_pager, msg, Snackbar.LENGTH_LONG).setAction("Action", null).show()
    }
    private fun checkAccount() {
        getSekolah()
        val user = SchoolApp.user?.currentUser
        if(user?.email == null)
            login()
        else {
            userRegister("current", user)
        }
    }

    private fun getSekolah() {

        SchoolApp.rest!!.getSekolah( object: Callback<ResponSchool>{
            override fun onError(error: WaspError?) {

            }

            override fun onSuccess(response: Response?, t: ResponSchool?) {
                val data = JSONObject(response?.body)
                SchoolApp.prefs!!.saveJSONObject("school","school",data)

            }
        })
    }

    private fun getProfile(token: String) {
        SchoolApp.rest!!.getProfile(token,object: Callback<ResponProfile>{
            override fun onError(error: WaspError?) {
                newsAdapter(DbLocal.newsList())
                snackBar("Network Failed...")
            }
            override fun onSuccess(response: Response?, t: ResponProfile?) {
                val data = JSONObject(response!!.body)
                PfUtil.saveJsonObject("user","profile",data)
                if(t!!.teacher.isNotEmpty()){
                    getParentUser(t.teacher[0].sekolah)
                    fab.show()
                }
                else
                    fab.hide()
                getNews(token)
            }
        })
    }
    fun getNews(token: String){

        SchoolApp.rest?.getNews(token, object : Callback<ResponNews> {
            override fun onError(error: WaspError?) {
                toast("Network Failed")
            }
            override fun onSuccess(response: Response?, t: ResponNews?) {
                val data = JSONObject(response?.body)
                SchoolApp.prefs?.saveJSONObject("school", "news", data)
                newsAdapter(t!!.data)
            }
        })
    }
    fun newsAdapter(data: ArrayList<NewsModel>){

        val home = pageAdapter.getFragment(0) as HomeFragment
        val news = data.reversed().filter { it.topic == "Umum" } as  ArrayList<NewsModel>
        home.updateAdapter(news)

        val activities = pageAdapter.getFragment(1) as HomeFragment
        activities.updateAdapter(newsFilter(data))
    }
    private fun login(){
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .build(),
                RC_SIGN_IN)

    }

    private fun userRegister(mode: String ,user: FirebaseUser) {

        val map = HashMap<String,String>()

        OneSignal.idsAvailable { userId, _ ->
            OneSignal.sendTag("email",user.email)
            map.put("name",user.displayName!!)
            map.put("email",user.email!!)
            map.put("uid",userId)
            map.put("token", userId)
        }
        SchoolApp.rest!!.getUser(map, object : Callback<ResponUser> {
            override fun onSuccess(response: Response?, t: ResponUser?) {
                val userRef = JSONObject(response!!.body)
                SchoolApp.prefs!!.saveJSONObject("user", "user", userRef)
                getProfile(userRef.getString("token"))
                if (mode == "new") {
                    startActivityForResult<Account>(RC_PROFILE)
                }
            }

            override fun onError(error: WaspError?) {

            }
        })

    }

    private fun createTab() {
        pageAdapter = TabPageAdapter(pageTitle,supportFragmentManager)
        view_pager.adapter = pageAdapter
        view_pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab_layout))
        tab_layout.apply({
            setupWithViewPager(view_pager)
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {

                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if(tab?.position == 0)
                        toolbar.title = "Informasi Sekolah"
                    else
                        toolbar.title = "Kegiatan Siswa"
                }
            })
        })

        tab_layout.getTabAt(0)?.icon = resources.getDrawable(R.drawable.tab_icon_news)
//        tab_layout.getTabAt(0)?.text = ""
        tab_layout.getTabAt(1)?.icon = resources.getDrawable(R.drawable.tab_icon_person1)
//        tab_layout.getTabAt(1)?.text = "
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == RC_SIGN_IN) {
            if (resultCode == ResultCodes.OK) {
                val user = SchoolApp.user?.currentUser
                if(user != null)
                    userRegister("new",user)
            }
            else{
               finish()
            }
        }

        if(requestCode == RC_PROFILE){
            if (resultCode == ResultCodes.OK) {
                getNews(DbLocal.token())
            }

        }
        if(requestCode == RC_POST){
            if (resultCode == ResultCodes.OK) {
                getNews(DbLocal.token())
            }

        }
    }

    fun newsFilter(news: ArrayList<NewsModel>): ArrayList<NewsModel>{
        if(news.isNotEmpty()){
            val list = ArrayList<String>()
            val berita = news.filterNot { it.topic in listOf("Umum","Public") }.reversed() as ArrayList<NewsModel>
            val profile = DbLocal.getProfile()
            val data: ArrayList<NewsModel>
            if( profile!!.teacher.count() > 0){
                list.addAll(profile.child.map { it.kelas } as ArrayList<String>)
                data = berita.filter { it.sekolah == profile.user.teacher.sekolah || it.topic in list  } as ArrayList<NewsModel>
            }
            else {
                list.addAll(profile.child.map { it.kelas } as ArrayList<String>)
                data = berita.filter { it.topic in list || it.userId == profile.user.email  }  as ArrayList<NewsModel>
            }

            return data
        }
        else{
            return ArrayList<NewsModel>()
        }

    }
    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
//            super.onBackPressed()
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_refresh){
            getNews(DbLocal.token())
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        if (id == R.id.nav_account) {
            startActivityForResult<Account>(RC_PROFILE,"post" to "account","id" to "0")

        } else if (id == R.id.nav_logut) {
            SchoolApp.user?.signOut()
            PfUtil.clear("user","user")
            PfUtil.clear("user","profile")
            PfUtil.clear("school","news")
            PfUtil.clear("post","activity")
            PfUtil.clear("post","news")
            PfUtil.clear("post","picture")
            login()
        }

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onMessage(message: String) {
        info { message }
        getNews(DbLocal.token())
    }

    fun getParentUser(school: String){
        SchoolApp.rest?.postParent(FilterModel(FilterArg(school)), object: Callback<ResponUsers>{
            override fun onSuccess(response: Response?, t: ResponUsers?) {
                val data = JSONObject(response?.body)
                PfUtil.saveJsonObject("user","parent",data)

            }

            override fun onError(error: WaspError?) {
                snackBar("Network failed, try again later..")

            }
        })
    }


}
