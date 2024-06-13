package com.comman.gallerypicker.Utils

import androidx.recyclerview.widget.DiffUtil
import com.comman.gallerypicker.models.Album
import com.comman.gallerypicker.models.MediaItemObj

object DiffUtils {

    val MEDIA_ITEM_DIFF_CALLBACK = object : DiffUtil.ItemCallback<MediaItemObj>() {
        override fun areItemsTheSame(oldItemObj: MediaItemObj, newItemObj: MediaItemObj): Boolean =
            oldItemObj.id == newItemObj.id

        override fun areContentsTheSame(
            oldItemObj: MediaItemObj,
            newItemObj: MediaItemObj
        ): Boolean =
            oldItemObj == newItemObj
    }

    val ALBUM_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean =
            oldItem == newItem
    }

    val FOR_YOU_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean =
            oldItem == newItem
    }

    val INT_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean =
            oldItem == newItem
    }

    val STRING_DIFF_CALLBACK = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
            oldItem == newItem
    }


}