package ru.netology.nmedia.activity

import android.annotation.SuppressLint
import android.widget.ImageView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R

@SuppressLint("CheckResult")
fun ImageView.load(url: String) {
    val BASE_URL = "http://10.0.2.2:9999/media"

    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.ic_loading_100dp)
        .error(R.drawable.ic_error_100dp)
        .timeout(10_000)
        .into(this)
}
