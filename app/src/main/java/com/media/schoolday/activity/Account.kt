package com.media.schoolday.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.media.schoolday.R
import com.media.schoolday.SchoolApp
import com.media.schoolday.models.*
import com.media.schoolday.utility.DbLocal
import com.media.schoolday.utility.PfUtil
import com.orhanobut.wasp.Callback
import com.orhanobut.wasp.Response
import com.orhanobut.wasp.WaspError
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.list_child_profile.view.*
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.json.JSONObject

class Account : AppCompatActivity() {
    lateinit var progres: ProgressDialog
    val profile = ArrayList<String>()
    lateinit var adapterChild : ArrayAdapter<String>
    private var responStatus = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_profile)
        progres = ProgressDialog(this@Account)
        createUi()
    }

    fun createUi(){

        adapterChild = object : ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1, profile) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                return super.getView(position, convertView, parent)
            }
        }
        adapterChild.setNotifyOnChange(true)
        getProfile()

        tvProfileEmail.text = DbLocal.getProfile()?.user?.email
        tvProfileName.text = DbLocal.getProfile()?.user?.name
        listAccountProfile.adapter = adapterChild

        tvAccountAnak.setOnClickListener { registrasi() }
        val user = SchoolApp.user?.currentUser
        tvProfileEmail.text = user?.email
        tvProfileName.text = user?.displayName
        ibAccountActivitySave.setOnClickListener {
            val data = Intent()
            data.putExtra("title",title)
            setResult(Activity.RESULT_OK,data)
            finish()
        }
    }

    fun getArrayData(data: ResponProfile?): ArrayList<String>{
        val listArray = ArrayList<String>()
        with(listArray){
            data?.child?.forEach { add(it.nama) }
            data?.teacher?.forEach { add(it.nama) }
        }
        return listArray
    }

    fun registrasi(){
//        val sekolah = ArrayList<String>()
//        var accoutRegister :String? = null
        val sekolah = DbLocal.schoolList()?.map { it.nama } as ArrayList<String>

        val account = listOf("Orang tua","Guru")

        selector("Pilih Accout",account) { i ->
            val register = account[i]
            selector("Pilih Sekolah",sekolah){i ->

                val cari = FilterModel(FilterArg(sekolah[i]))

                alert {
                    customView {
                        verticalLayout {
                            toolbar {
                                id = R.id.dialog_toolbar
                                lparams(width = matchParent, height = wrapContent)
                                backgroundColor = ContextCompat.getColor(ctx, R.color.colorPrimary)
                                setTitleTextColor(ContextCompat.getColor(ctx, android.R.color.white))
                                if(register == account[0])
                                    title = "Nomor Nis anak"
                                else
                                    title = "Nomor Handphone anda"
                            }

                            val nis = editText { padding = dip(20) }
                            positiveButton("REGISTER") {
                                val arg = nis.text.toString()

                                if(register == (account[0]))
                                    getAnak(cari, arg)
                                else
                                    getGuru(cari, arg)

                            }
                        }
                    }
                }.show()
            }
        }


    }

    private fun getGuru(filter: FilterModel, telp: String) {

        SchoolApp.rest!!.postGuru(filter, object : Callback<Teachers> {
            override fun onSuccess(response: Response?, t: Teachers?) {
                val data = JSONObject(response!!.body)
                PfUtil.saveJsonObject("school","guru",data)
                val cari = t!!.data.filter { it.telp == (telp) }
                with(cari) {
                    when {
                        isNotEmpty() -> {
                            forEach {
                                alert(it.nama + ", Proses ?") {
                                    yesButton { updateProfileGuru(ProfileGuru("update", Guru(it.id, it.sekolah))) }
                                }.show()
                            }
                        }
                        else -> {
                            alert("data tidak ditemukan") { yesButton {} }.show()

                        }
                    }
                }
            }

            override fun onError(error: WaspError?) {
                alert("network error"){yesButton {  }}.show()
            }
        })
    }

    private fun getAnak(filter: FilterModel, nik: String) {
        progres.show()
        SchoolApp.rest!!.postSiswa(filter, object : Callback<Students> {
            override fun onSuccess(response: Response?, t: Students?) {
                progres.hide()
                val data = JSONObject(response!!.body)
                PfUtil.saveJsonObject("school", "siswa", data)
                val cari = t!!.data.filter { it.nis == (nik) }
                with(cari) {
                    when {
                        isNotEmpty() -> {
                            forEach {
                                alert(it.nama + ", Proses ?") {
                                    yesButton { updateProfileAnak(ProfilAnak("update", Anak(it.sekolah, it.nis))) }
                                }.show()
                            }
                        }
                        else -> {
                            alert("data tidak ditemukan") { yesButton {} }.show()

                        }
                    }
                }
            }

            override fun onError(error: WaspError?) {
                progres.hide()
                toast("Network failed, please try again later")
            }
        })
    }

    private fun updateProfileAnak(anak: ProfilAnak){
        progres.show()
        SchoolApp.rest!!.putProfileAnak(DbLocal.token(),anak, object : Callback<Profile> {
            override fun onError(error: WaspError?) {
                progres.hide()
                toast("Network failed, please try again later")
            }
            override fun onSuccess(response: Response?, t: Profile?) {
                progres.hide()
                responStatus = true
                getProfile()
//                alert("Profile sudah di update"){yesButton {}}.show()

            }
        })
    }

    private fun updateProfileGuru(guru: ProfileGuru){
        progres.show()
        SchoolApp.rest!!.putProfileGuru(DbLocal.token(),guru, object : Callback<Profile> {
            override fun onError(error: WaspError?) {
                progres.hide()
                alert("Network Failed"){yesButton {}}.show()
            }
            override fun onSuccess(response: Response?, t: Profile?) {
                progres.hide()
                responStatus = true
                getProfile()
//                alert("Profile sudah di update"){yesButton {}}.show()

            }
        })
    }

    fun getProfile() {
        SchoolApp.rest!!.getProfile(DbLocal.token(),object: Callback<ResponProfile> {
            override fun onError(error: WaspError?) {
            }

            override fun onSuccess(response: Response?, t: ResponProfile?) {
                val data = JSONObject(response!!.body)
                PfUtil.saveJsonObject("user","profile",data)
//                profile.clear()
//                profile.addAll(getArrayData(t))
//                adapterChild.notifyDataSetChanged()
                addAccount(t!!)
//                val accout = supportFragmentManager.findFragmentByTag("account") as AcountFragment
//                accout.updateAdapter()

            }
        })
    }
    fun addAccount(list: ResponProfile) {
        if (list != null) {
            list.child.forEach {

                val listItem = layoutInflater.inflate(R.layout.list_child_profile, null)
                val header = TextView(this)
                header.text = "Profile anak"
                header.padding = 8
                with(it) {
                    listItem.tvAccounProfileSchool.text = sekolah
                    listItem.tvAccounProfileName.text = nama
                    listItem.tvAccounProfileNip.text = "Nis : $nis"
                    listItem.imageView2.setOnClickListener {
                        alert("Remove Account $nama ?") { yesButton {
                            updateProfileAnak(ProfilAnak("delete", Anak(nis, sekolah)))
                        } }.show()
                    }
                }
                linearAccountParent.addView(listItem)
            }

            list.teacher.forEach {
                val listItem = layoutInflater.inflate(R.layout.list_child_profile, null)
                val header = TextView(this)
                header.text = "Profile guru"
                header.padding = 8
                with(it) {
                    listItem.tvAccounProfileSchool.text = sekolah
                    listItem.tvAccounProfileName.text = nama
                    listItem.tvAccounProfileNip.text = "Telp : $telp"
                    listItem.imageView2.setOnClickListener {
                        alert("Remove Account $nama ?") { yesButton {
                            updateProfileGuru(ProfileGuru("update", Guru("0", sekolah)))
                        } }.show()
                    }
                }
                linearAccountTeacher.addView(header)
                linearAccountTeacher.addView(listItem)
            }

        }
    }
}
