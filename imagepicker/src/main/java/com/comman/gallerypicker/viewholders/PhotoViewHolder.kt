package com.comman.gallerypicker.viewholders

import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import com.comman.gallerypicker.Utils.setThumbnail
import com.comman.gallerypicker.databinding.CardPhotoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PhotoViewHolder(val binding: CardPhotoBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun setThumbnail(uri: Uri?) {
        CoroutineScope(Dispatchers.Main).launch {
            uri?.let {
                binding.thumbnail.setThumbnail(it)
            }
        }
    }

    fun setThumbnail(path: String?) {
        path?.let {
            binding.thumbnail.setThumbnail(it)
        }
    }


}