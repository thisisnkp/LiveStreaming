package com.comman.gallerypicker.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.comman.gallerypicker.Utils.DiffUtils.ALBUM_DIFF_CALLBACK
import com.comman.gallerypicker.Utils.setThumbnail
import com.comman.gallerypicker.databinding.CardAlbumBinding
import com.comman.gallerypicker.models.Album
import com.comman.gallerypicker.models.MediaItemObj

class AlbumAdapter(private val ac: Activity, val listener: OnAdapterClickListener) :
    ListAdapter<Album, AlbumAdapter.AlbumViewHolder>(ALBUM_DIFF_CALLBACK) {

    class AlbumViewHolder(private val binding: CardAlbumBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * set thumbnails
         */
        fun setThumbnails(mediaItems: List<MediaItemObj>) {
            val size = mediaItems.size
            when {
                size == 1 -> {
                    val item1 = mediaItems[0]
                    //
                    binding.layout1.thumbnail.setThumbnail(item1.pathRaw!!)
                    //
                    showIconPlay(item1.isVideo(), binding.layout1.play)
                }

                size == 2 -> {
                    val item1 = mediaItems[0]
                    val item2 = mediaItems[1]
                    //
                    binding.layout1.thumbnail.setThumbnail(item1.pathRaw!!)
                    binding.layout2.thumbnail.setThumbnail(item2.pathRaw!!)
                    //
                    showIconPlay(item1.isVideo(), binding.layout1.play)
                    showIconPlay(item2.isVideo(), binding.layout2.play)
                }

                size == 3 -> {
                    val item1 = mediaItems[0]
                    val item2 = mediaItems[1]
                    val item3 = mediaItems[2]
                    //
                    binding.layout1.thumbnail.setThumbnail(item1.pathRaw!!)
                    binding.layout2.thumbnail.setThumbnail(item2.pathRaw!!)
                    binding.layout3.thumbnail.setThumbnail(item3.pathRaw!!)
                    //
                    showIconPlay(item1.isVideo(), binding.layout1.play)
                    showIconPlay(item2.isVideo(), binding.layout2.play)
                    showIconPlay(item3.isVideo(), binding.layout3.play)
                }

                size > 3 -> {
                    val item1 = mediaItems[0]
                    val item2 = mediaItems[1]
                    val item3 = mediaItems[2]
                    val item4 = mediaItems[3]
                    //
                    binding.layout1.thumbnail.setThumbnail(item1.pathRaw!!)
                    binding.layout2.thumbnail.setThumbnail(item2.pathRaw!!)
                    binding.layout3.thumbnail.setThumbnail(item3.pathRaw!!)
                    binding.layout4.thumbnail.setThumbnail(item4.pathRaw!!)
                    //
                    showIconPlay(item1.isVideo(), binding.layout1.play)
                    showIconPlay(item2.isVideo(), binding.layout2.play)
                    showIconPlay(item3.isVideo(), binding.layout3.play)
                    showIconPlay(item4.isVideo(), binding.layout4.play)
                }
            }
        }

        /**
         * show-hide icon
         */
        private fun showIconPlay(isVideo: Boolean, icPlay: ImageButton) {
            icPlay.visibility = if (isVideo) View.VISIBLE else View.GONE
        }

        /***
         * remove thumbnails from imageview
         */
        fun clearThumbnails(context: Context) {
            Glide.with(context).clear(binding.layout1.thumbnail)
            Glide.with(context).clear(binding.layout2.thumbnail)
            Glide.with(context).clear(binding.layout3.thumbnail)
            Glide.with(context).clear(binding.layout4.thumbnail)
        }

        /**
         * hide icons
         */
        fun clearIcPlay() {
            binding.layout1.play.visibility = View.GONE
            binding.layout2.play.visibility = View.GONE
            binding.layout3.play.visibility = View.GONE
            binding.layout4.play.visibility = View.GONE
        }

        /***
         * set album name
         */
        fun setName(name: String) {
            binding.txtName.text = name
        }

        /**
         * set number of items
         */
        fun setNrItems(nrItems: Int) {
            binding.txtNrItems.text = "$nrItems"
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = AlbumViewHolder(
        CardAlbumBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = getItem(position)
        val mediaItems = album.mediaItems
        holder.setThumbnails(mediaItems)
        holder.setName(album.name)
        holder.setNrItems(mediaItems.size)

        holder.itemView.setOnClickListener {
            //set items
            listener.onAlbumClick(album)
        }

        holder.itemView.setOnLongClickListener {
            listener.onAlbumLongClick(album)
            true
        }
    }

    override fun onViewRecycled(holder: AlbumViewHolder) {
        super.onViewRecycled(holder)
        holder.clearThumbnails(ac)
        holder.clearIcPlay()
    }

    interface OnAdapterClickListener {
        fun onAlbumClick(album: Album)
        fun onAlbumLongClick(album: Album)
    }

}
