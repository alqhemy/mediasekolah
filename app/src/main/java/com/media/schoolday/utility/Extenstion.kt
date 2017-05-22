package com.media.schoolday.utility

/**
 * Created by yosi on 06/05/2017.
 */


import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.media.schoolday.R
import com.squareup.picasso.Picasso

//import com.bumptech.glide.Glide

/**
 * Created by yosi on 20/04/2017.
 */

//fun ImageView.loadUrl(url:String){
//    Glide.with(context)
//            .load(url)
//            .into(this)
//}

fun ViewGroup.inflate(layoutRes: Int): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, false)
}

fun ViewGroup.inflate(ctx: Context, layoutRes: Int): View {
    return LayoutInflater.from(ctx).inflate(layoutRes, this, false)
}

fun ImageView.loadImg(imageUrl: String) {
    if (TextUtils.isEmpty(imageUrl)) {
        Picasso.with(context).load(R.drawable.ic_menu_gallery).into(this)
    } else {
        Picasso.with(context).load(imageUrl).into(this)
    }
}