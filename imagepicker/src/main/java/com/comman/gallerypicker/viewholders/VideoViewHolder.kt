package com.comman.gallerypicker.viewholders

import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.comman.gallerypicker.Utils.getDuration
import com.comman.gallerypicker.Utils.setThumbnail
import com.comman.gallerypicker.databinding.CardVideoBinding

class VideoViewHolder(val binding: CardVideoBinding) :
    RecyclerView.ViewHolder(binding.root) {

    /**
     * set image
     */
    fun setThumbnail(uri: Uri?) {
        uri?.let {
            binding.thumbnail.setThumbnail(it)
        }
    }

    fun setThumbnail(path: String?) {
        path?.let {
            binding.thumbnail.setThumbnail(it)
        }
    }

    /**
     * show-hide duration text
     */
    fun showTxtDuration(spanCount: Int) {
        binding.txtDuration.visibility = if (spanCount > 3) View.GONE else View.VISIBLE
        binding.txtDuration.visibility = View.GONE
    }

    /**
     * show-hide play icon
     */
    fun showIconPlay(spanCount: Int) {
        binding.play.visibility = if (spanCount > 6) View.GONE else View.VISIBLE
    }

    /**
     * set video duration
     */
    fun setDuration(videoDuration: Long) {
        binding.txtDuration.text = videoDuration.getDuration()
    }

}