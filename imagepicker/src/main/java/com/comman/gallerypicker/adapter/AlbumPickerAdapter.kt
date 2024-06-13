package com.comman.gallerypicker.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.comman.gallerypicker.PickerMainActivity
import com.comman.gallerypicker.PickerMainActivity.Companion.isSingleSelection
import com.comman.gallerypicker.PickerMainActivity.Companion.selectedMediaItems
import com.comman.gallerypicker.Utils.DiffUtils
import com.comman.gallerypicker.databinding.CardPhotoBinding
import com.comman.gallerypicker.databinding.CardPickerHeaderBinding
import com.comman.gallerypicker.databinding.CardVideoBinding
import com.comman.gallerypicker.models.MediaItemObj
import com.comman.gallerypicker.viewholders.PhotoViewHolder
import com.comman.gallerypicker.viewholders.TitleViewHolder
import com.comman.gallerypicker.viewholders.VideoViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlbumPickerAdapter(val activity: Activity) :
    ListAdapter<MediaItemObj, RecyclerView.ViewHolder>(DiffUtils.MEDIA_ITEM_DIFF_CALLBACK),
    Filterable, ListPreloader.PreloadModelProvider<MediaItemObj> {

    private var mediaItemsSearch = mutableListOf<MediaItemObj>()

    val fullRequest = Glide.with(activity)
        .asDrawable()
        .centerCrop();

    val thumbnailRequest = Glide.with(activity)
        .asDrawable()
        .centerCrop()
        .override(200, 200);

    var selectedPosition: MediaItemObj? = null
    var prevPosition: MediaItemObj? = null

    var span = 3
        set(value) {
            field = value
            notifyItemRangeChanged(0, itemCount)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> {
                return VideoViewHolder(
                    CardVideoBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }

            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> {
                return PhotoViewHolder(
                    CardPhotoBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }

            else -> {
                return TitleViewHolder(
                    CardPickerHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mediaItem = getItem(position)

        CoroutineScope(Dispatchers.Main).launch {
            when (holder) {
                is PhotoViewHolder -> {
                    holder.setThumbnail(mediaItem.uri)

                    holder.binding.relativeCheck.visibility = View.GONE
                    for (i in selectedMediaItems.indices) {
                        if (selectedMediaItems[i].id == mediaItem.id) {
                            holder.binding.relativeCheck.visibility = View.VISIBLE
                            break
                        }
                    }

                    holder.itemView.setOnClickListener {

                        if (isSingleSelection) {


                            selectedMediaItems.clear()


                            if (holder.binding.relativeCheck.visibility == View.GONE) {
                                selectedMediaItems.add(mediaItem)

                                PickerMainActivity.mTVCount?.text =
                                    "${selectedMediaItems.size} Selected"

                                holder.binding.relativeCheck.visibility = View.VISIBLE
                            } else {
                                selectedMediaItems.remove(mediaItem)

                                PickerMainActivity.mTVCount?.text =
                                    "${selectedMediaItems.size} Selected"

                                holder.binding.relativeCheck.visibility = View.GONE
                            }

                            notifyItemChanged(currentList.indexOf(selectedPosition))
                            notifyItemChanged(currentList.indexOf(prevPosition))

                            selectedPosition = mediaItem
                            prevPosition = selectedPosition
                        } else {


                            if (holder.binding.relativeCheck.visibility == View.GONE) {

                                if (selectedMediaItems.size == 6) {
                                    Toast.makeText(
                                        activity,
                                        "max 6 images allowed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@setOnClickListener
                                }

                                selectedMediaItems.add(mediaItem)

                                PickerMainActivity.mTVCount?.text =
                                    "${selectedMediaItems.size} Selected"

                                holder.binding.relativeCheck.visibility = View.VISIBLE
                            } else {
                                selectedMediaItems.remove(mediaItem)

                                PickerMainActivity.mTVCount?.text =
                                    "${selectedMediaItems.size} Selected"

                                holder.binding.relativeCheck.visibility = View.GONE
                            }
                        }
                    }

                }

                is VideoViewHolder -> {

                    holder.setThumbnail(mediaItem.uri)
                    holder.setDuration(mediaItem.duration)
                    holder.showTxtDuration(span)
                    holder.showIconPlay(span)

                    holder.binding.relativeCheck.visibility = View.GONE

                    for (i in selectedMediaItems.indices) {
                        if (selectedMediaItems[i].id == mediaItem.id) {
                            holder.binding.relativeCheck.visibility = View.VISIBLE
                            break
                        }
                    }

                    holder.itemView.setOnClickListener {
                        if (holder.binding.relativeCheck.visibility == View.GONE) {
                            selectedMediaItems.add(mediaItem)
                            holder.binding.relativeCheck.visibility = View.VISIBLE

                        } else {
                            selectedMediaItems.remove(mediaItem)
                            holder.binding.relativeCheck.visibility = View.GONE

                        }
                    }

                }

                is TitleViewHolder -> {
                    holder.setTitle(mediaItem.name)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).mediaType
    }

    fun setItems(mediaItems: List<MediaItemObj>) {
        this.mediaItemsSearch = mediaItems.toMutableList()
    }

    override fun getFilter(): Filter {
        return itemsFilter
    }

    private val itemsFilter = object : Filter() {
        override fun performFiltering(charSequence: CharSequence?): FilterResults {
            //create empty list
            val filteredList = mutableListOf<MediaItemObj>()
            //check
            if (charSequence!!.isNotEmpty()) {
                //check if any media item contain text passed by user and add to list if contains
                for (video in mediaItemsSearch) {
                    val txtFilter = charSequence.toString().lowercase()
                    if (video.name.lowercase().contains(txtFilter) ||
                        video.getDayMonthYear().lowercase().contains(txtFilter) ||
                        video.getMonthYear().lowercase().contains(txtFilter) ||
                        video.getYear().lowercase().contains(txtFilter)
                    ) {
                        filteredList.add(video)
                    }
                }
            }
            //return list
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence?, filterResults: FilterResults?) {
            //submit new list
            submitList(filterResults?.values as MutableList<MediaItemObj>)
        }
    }

    override fun getPreloadItems(position: Int): MutableList<MediaItemObj> {
        return currentList.subList(position, position + 1);
    }

    override fun getPreloadRequestBuilder(item: MediaItemObj): RequestBuilder<*>? {
        return fullRequest.clone().thumbnail(thumbnailRequest.clone().load(item)).load(item);
    }
}