package com.media.schoolday

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
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
import com.media.schoolday.activity.NewsActivity
import com.media.schoolday.adapter.TabPageAdapter
import com.media.schoolday.fragment.HomeFragment
import com.media.schoolday.models.model.*
import com.media.schoolday.utility.DbLocal
import com.media.schoolday.utility.PfUtil
import com.orhanobut.wasp.Callback
import com.orhanobut.wasp.Response
import com.orhanobut.wasp.WaspError
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.*
import org.json.JSONObject

class MainActivity : AppCompatActivity(),AnkoLogger,
        NavigationView.OnNavigationItemSelectedListener {
    private val  RC_SIGN_IN = 123
    private val  RC_PROFILE = 112
    lateinit var fab:FloatingActionButton
    lateinit var pageAdapter: TabPageAdapter
    val pageTitle = arrayOf("Berita","Aktivitas")
    lateinit  var progressDialog: ProgressDialog
//    lateinit var userId: FirebaseAuth
//    private val realm = Realm.getDefaultInstance()

    interface OnItemClickListener {
        fun OnDataUpdate(args: ArrayList<NewsModel>)
    }
    var callback: OnItemClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            newPost()
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
        }

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)
//        userId = FirebaseAuth.getInstance()
        createTab()
        checkAccount()
    }

    private fun checkAccount() {
        val user = SchoolApp.user?.currentUser
        if(user == null)
            login()
        else {
            getSekolah()
            userRegister("current", user)
        }
    }

    private fun getSekolah() {

        SchoolApp.rest!!.getSekolah( object: Callback<ResponSchool>{
            override fun onError(error: WaspError?) {
                toast("Network Failed")
            }

            override fun onSuccess(response: Response?, t: ResponSchool?) {
                val data = JSONObject(response?.body)
                SchoolApp.prefs!!.saveJSONObject("sekolah","sekolah",data)

            }
        })
    }

    private fun getProfile(token: String) {
        SchoolApp.rest!!.getProfile(token,object: Callback<ResponProfile>{
            override fun onError(error: WaspError?) {
//                    newsAdapter(DbLocal.newsList())
            }
            override fun onSuccess(response: Response?, t: ResponProfile?) {
                val data = JSONObject(response!!.body)
                PfUtil.saveJsonObject("user","profile",data)
                if(t!!.teacher.size > 0){
                    fab.show()
                }
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
                SchoolApp.prefs?.saveJSONObject("sekolah", "news", data)
                newsAdapter(t!!.data)
            }
        })
    }
    fun newsAdapter(data: ArrayList<NewsModel>){
        val home = pageAdapter.getFragment(0) as HomeFragment
        home.updateAdapter(data)

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
        map.put("name",user.displayName!!)
        map.put("email",user.email!!)
        map.put("uid",user.uid)
//
        SchoolApp.rest!!.getUser(map, object : Callback<ResponUser> {
            override fun onSuccess(response: Response?, t: ResponUser?) {
                val userRef = JSONObject(response!!.body)
                SchoolApp.prefs!!.saveJSONObject("user", "user", userRef)
                getProfile(userRef.getString("token"))
                if (mode == "new") {
                    newPost()
                }
            }

            override fun onError(error: WaspError?) {
                toast("Network Failed")
            }
        })

    }
    fun newPost(){
        startActivityForResult<NewsActivity>(RC_PROFILE, "post" to "new","id" to "0")
        getNews(DbLocal.token())
    }
    private fun createTab() {
        pageAdapter = TabPageAdapter(pageTitle,supportFragmentManager)
        view_pager.adapter = pageAdapter
        view_pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab_layout))
        tab_layout.apply({
            setupWithViewPager(view_pager)
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {}

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabSelected(tab: TabLayout.Tab?) {}
            })
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == RC_SIGN_IN) {
            if (resultCode == ResultCodes.OK) {

                val user = SchoolApp.user?.currentUser
                info { "register user" }
                info { user?.displayName + " : " + user?.displayName +" : " + user?.uid}
                if(user != null)
                    userRegister("new",user)
            }
            else{
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false)
                                .build(),
                        RC_SIGN_IN)
            }
        }

        if(requestCode == RC_PROFILE){
            val title = data?.extras?.get("title")
            if(resultCode == ResultCodes.OK){

//                val title = data?.extras?.get("title")
                if(title == "account") {
                    info { title }
//                    updateHomeNew()
                }
            }
        }
    }

    fun updateHomeNew(){
        getProfile(DbLocal.token())
    }


    fun newsFilter(news: ArrayList<NewsModel>): ArrayList<NewsModel>{
        val list = ArrayList<String>()
        if (DbLocal.getProfile()?.child!!.isNotEmpty()){
            list.addAll(DbLocal.getProfile()!!.child.map { it.kelas } as ArrayList<String>)
        }
        info { list.toString() }
        val filter = news.filter { it.topic in list || it.userId == DbLocal.getProfile()!!.user.email } as ArrayList<NewsModel>
        return filter
    }
    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
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


        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        if (id == R.id.nav_account) {
            startActivity<NewsActivity>("post" to "account","id" to "0")
        } else if (id == R.id.nav_logut) {
            SchoolApp.user?.signOut()
            PfUtil.clear("user","user")
            PfUtil.clear("user","profile")
            PfUtil.clear("sekolah","news")
            login()
        }

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
}
