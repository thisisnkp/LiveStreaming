package com.comman.gallerypicker.fragment

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.FixedPreloadSizeProvider
import com.comman.gallerypicker.PickerMainActivity.Companion.albumName
import com.comman.gallerypicker.PickerMainActivity.Companion.mediaItems
import com.comman.gallerypicker.R
import com.comman.gallerypicker.Utils.CursorUtils
import com.comman.gallerypicker.adapter.AlbumPickerAdapter
import com.comman.gallerypicker.databinding.FragmentAlbumPickerInfoBinding
import com.comman.gallerypicker.models.MediaItemObj
import com.comman.gallerypicker.viewholders.PhotoViewHolder
import com.comman.gallerypicker.viewholders.VideoViewHolder
import com.comman.gallerypicker.viewmodels.AlbumPickerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class AlbumPickerInfoFragment : Fragment() {

    companion object {
        fun newInstance() = AlbumPickerInfoFragment()
    }

    private lateinit var viewModel: AlbumPickerViewModel
    private lateinit var binding: FragmentAlbumPickerInfoBinding

    private val albumList = ArrayList<String>()
    private val albumPathList = ArrayList<String>()

    private val adapter by lazy { AlbumPickerAdapter(requireActivity()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[AlbumPickerViewModel::class.java]
        binding = FragmentAlbumPickerInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        albumName = arguments?.getString("albumNameKey") ?: ""

        loadVaultAlbum()
        loadAlbum()
        setObservers()
        val mLayoutManager =
            GridLayoutManager(requireContext(), 3, GridLayoutManager.VERTICAL, false)
        mLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> 1
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> 1
                    else -> 3
                }
            }
        }
        binding.recyclerView.layoutManager = mLayoutManager

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.addRecyclerListener { holder ->
            if (holder is PhotoViewHolder) {
                val photoViewHolder: PhotoViewHolder = holder as PhotoViewHolder
                Glide.with(this@AlbumPickerInfoFragment).clear(photoViewHolder.binding.thumbnail)
            } else if (holder is VideoViewHolder) {
                val videoViewHolder: VideoViewHolder = holder as VideoViewHolder
                Glide.with(this@AlbumPickerInfoFragment).clear(videoViewHolder.binding.thumbnail)
            }
        }
        val heightCount: Int = resources.displayMetrics.heightPixels / 200
        binding.recyclerView.recycledViewPool.setMaxRecycledViews(0, 15 * heightCount * 2)
        binding.recyclerView.setItemViewCacheSize(0);
        binding.recyclerView.adapter = adapter

        val preloadSizeProvider: FixedPreloadSizeProvider<MediaItemObj> =
            FixedPreloadSizeProvider(200, 200)
        val preloader = RecyclerViewPreloader(
            Glide.with(this), adapter,
            preloadSizeProvider, 10
        )
        binding.recyclerView.addOnScrollListener(preloader)
        binding.recyclerView.adapter = adapter

        ContextCompat.getDrawable(requireContext(), R.drawable.custom_bg_primary)
            ?.let { FastScrollerBuilder(binding.recyclerView).setThumbDrawable(it).build() }

    }

    private fun loadAlbum() {

        viewModel.mediaItems.observe(viewLifecycleOwner, Observer {
            viewModel.loadAlbums(it)
        })

        viewModel.loadMediaItems(CursorUtils.SELECTION_ALL)
    }

    private fun loadVaultAlbum() {
    }

    private fun setObservers() {

        viewModel.forYouItems.observe(viewLifecycleOwner, Observer {


//            lifecycleScope.launch(Dispatchers.Main) {
//                it.forEach {
//                    Log.d("TAG", "setObservers: forYouItems -> ${it.mediaItems.size}")
//                }
//            }
            lifecycleScope.launch(Dispatchers.Main) {
                val list = arrayListOf<MediaItemObj>()
                it.forEach {
                    list.add(
                        MediaItemObj(
                            0,
                            Uri.EMPTY,
                            it.name,
                            null,
                            null,
                            0L,
                            MediaStore.Files.FileColumns.MEDIA_TYPE_NONE,
                            "",
                            //0 = no duration
                            0L,
                            0L,
                            //to order media in favorites fragment
                            0L
                        )
                    )
                    list.addAll(it.mediaItems)
                }
                adapter.submitList(list)
            }
        })

        viewModel.loadDateWise(mediaItems)

    }

    override fun onStart() {
        super.onStart()
    }

}