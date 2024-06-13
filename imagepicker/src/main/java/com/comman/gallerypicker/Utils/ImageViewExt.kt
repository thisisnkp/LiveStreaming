package com.comman.gallerypicker.Utils

import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions

/**
 * show thumbnail with transition
 */
fun ImageView.setThumbnail(uri: Uri, withTransition: Boolean = true) {
    if (withTransition) {
        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .skipMemoryCache(true)
            .centerCrop()
            .dontAnimate()
            .dontTransform()
            .override(200, 200)
            .priority(Priority.IMMEDIATE)
            .encodeFormat(Bitmap.CompressFormat.PNG)
            .format(DecodeFormat.DEFAULT);

        Glide.with(context)
            .load(uri)
            .apply(requestOptions)
            .into(this@setThumbnail)
    } else {
        Glide.with(context)
            .load(uri)
            .into(this@setThumbnail)
    }


}

fun ImageView.setThumbnail(path: String, withTransition: Boolean = true) {
    if (withTransition) {
        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .override(200, 200)

        Glide.with(context)
            .load(path)
            .apply(requestOptions)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(this@setThumbnail)
    } else {
        Glide.with(context)
            .load(path)
            .into(this@setThumbnail)
    }
}

/**
 * apply filter or show default image
 */
fun ImageView.applyFilter(uri: Uri, transformation: Transformation<Bitmap>?) {
    if (transformation == null) {
        Glide.with(context)
            .load(uri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(this)
    } else {
        Glide.with(context)
            .load(uri)
            .apply(RequestOptions.bitmapTransform(transformation))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(this)
    }
}

/**
 * show image without cache
 */
fun ImageView.setThumbnailSkippingCache(uri: Uri) {
    Glide.with(context)
        .load(uri)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .skipMemoryCache(true)
        .into(this)
}