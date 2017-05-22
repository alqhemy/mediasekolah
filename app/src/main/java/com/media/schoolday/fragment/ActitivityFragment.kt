package com.media.schoolday.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import com.media.schoolday.R
import com.media.schoolday.models.model.Activity
import com.media.schoolday.models.model.PostActivities
import com.media.schoolday.models.model.Student
import com.media.schoolday.utility.DbLocal
import kotlinx.android.synthetic.main.fragment_news_activity.*
import kotlinx.android.synthetic.main.fragment_news_activity.view.*
import kotlinx.android.synthetic.main.input_item.view.*
import kotlinx.android.synthetic.main.list_view.view.*
import org.jetbrains.anko.selector
import org.jetbrains.anko.toast




/*
 * Created by yosi on 21/05/2017.
 */

class ActitivityFragment: Fragment(){

    private val listActivity = ArrayList<Activity>()
    interface OnItemClickListener {
        fun onAddActivity(args: PostActivities)
        fun onClickItem(arg: String)
        fun sendMessage(arg: String)
    }
    lateinit var views: View
    lateinit var root: View

    val ID_KEY = "id"
    var idKey: String? = null
    var callback: OnItemClickListener? = null

    companion object{

        fun newInstance(idNews: String): ActitivityFragment{
            val fragment = ActitivityFragment()
            val bundle = Bundle()
            fragment.idKey = idNews
            bundle.putString("id", idNews )
            fragment.arguments = bundle
            return fragment
        }

//        fun newInstance(): ActitivityFragment = newInstance()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        idKey = savedInstanceState?.getString("id")
//        context.toast(idKey.toString())
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        callback = activity as? OnItemClickListener
        context.toast(idKey.toString())
//        if(idKey == "activity"){
            root = inflater!!.inflate(R.layout.fragment_news_activity,container,false)
            activityEnry(root)
//        }
//        else {
//            root = inflater!!.inflate(R.layout.list_view,container,false)
//            listUser(root)
//        }

        return root
    }

    fun activityEnry(entry: View){
        listActivity()?.forEach {
            views = LayoutInflater.from(activity.baseContext).inflate(R.layout.input_item, null)
            if(it != "Umum"){
                val edit = EditText(context)
                with(edit){
                    hint = it
                    id = View.generateViewId()
                    tag = it
                }
                views.inputLayout.addView(edit)
            }
            root.linearActivityInput.addView(views)
        }
    }

    fun listUser(list: View){
        val student = DbLocal.studentList().filter { it.kelas == idKey }
        val adapter = ChildAdapter(context,student)
        list.simple_list.adapter = adapter

    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideKeyboard()
        view?.ibNewsActivityAnak?.setOnClickListener {
            hideKeyboard()
            val list = DbLocal.studentList()
                    .filter { it.kelas == idKey }
                    .map { it.nama }
            activity.selector("Kelas ${idKey}", list) { i ->
//                val aktivitas = activity.supportFragmentManager.findFragmentByTag("activity") as ActitivityFragment
                editAnak(list[i])
            }
//            callback?.onClickItem(idKey as String)

        }

    }
    fun addToList(){
        val listEntryActivity = ArrayList<Activity>()
        listActivity()?.forEach {
            if(it != "Umum"){
                val edit = root.findViewWithTag(it) as EditText
                val activity = Activity()
                activity.title = it
                activity.description = edit.text.toString()
                listEntryActivity.add(activity)
//                callback?.sendMessage(it +": "+ edit.text)
            }
        }
    }
    fun listActivity():ArrayList<String>?{

        val profile = DbLocal.getProfile()
        val sekolah = DbLocal.schoolList()
        if(sekolah != null) {
            val filter = sekolah.filter { it.nama == profile!!.teacher.first().sekolah }
            return filter.first().activity
        }
        else {
            return null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.fragment_menu,menu)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId){
            R.id.item_save -> {
                context.toast("test save")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    fun editAnak(nama: String){
        etNewsActivityAnak.text.insert(0,nama)
    }

    fun hideKeyboard(){
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}
class ChildAdapter(ctx: Context, student: List<Student>): ArrayAdapter<Student>(ctx,0, student){
    private val cx = ctx
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        val user = getItem(position)
        // Check if an existing view is being reused, otherwise inflate the view
        if(v != null){
            v = LayoutInflater.from(cx).inflate(android.R.layout.simple_list_item_2, parent, false)
        }
        val name = v?.findViewById(android.R.id.text1) as TextView
        val nis = v?.findViewById(android.R.id.text2) as TextView
        name.text = user.nama
        nis.text = user.nis
        v.setOnClickListener {
            cx.toast( user.nama)
        }
        // Return the completed view to render on screen
        return v
    }
}
