package com.comman.gallerypicker.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.comman.gallerypicker.PickerMainActivity
import com.comman.gallerypicker.R
import com.comman.gallerypicker.Utils.CursorUtils
import com.comman.gallerypicker.adapter.AlbumAdapter
import com.comman.gallerypicker.databinding.FragmentAlbumPickerBinding
import com.comman.gallerypicker.models.Album
import com.comman.gallerypicker.viewmodels.AlbumPickerViewModel
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class AlbumPickerFragment : Fragment() {

    companion object {
        fun newInstance() = AlbumPickerFragment().apply {

        }
    }

    private lateinit var viewModel: AlbumPickerViewModel
    private lateinit var binding: FragmentAlbumPickerBinding
    private val adapter by lazy {
        AlbumAdapter(requireActivity(), object : AlbumAdapter.OnAdapterClickListener {
            override fun onAlbumClick(album: Album) {
                openAlbum(album)
            }

            override fun onAlbumLongClick(album: Album) {

            }

        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[AlbumPickerViewModel::class.java]
        binding = FragmentAlbumPickerBinding.inflate(inflater, container, false)
        binding.inLayoutNoData.icon.setImageResource(R.drawable.ic_albums)
        binding.inLayoutNoData.txtNoData.text = getString(R.string.no_albums)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setObservers()

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerView.adapter = adapter

        ContextCompat.getDrawable(requireContext(), R.drawable.custom_bg_primary)
            ?.let { FastScrollerBuilder(binding.recyclerView).setThumbDrawable(it).build() }
    }

    private fun setObservers() {

        viewModel.mediaItems.observe(viewLifecycleOwner) { items ->
            viewModel.loadAlbums(items)
        }

        viewModel.albums.observe(viewLifecycleOwner) { albums ->
            binding.inLayoutNoData.root.visibility = if (albums.isEmpty())
                View.VISIBLE else View.GONE
            adapter.submitList(albums)
        }

        if (PickerMainActivity.isSingleSelection) {
            viewModel.loadMediaItems(CursorUtils.SELECTION_PHOTOS)
        } else {
            viewModel.loadMediaItems(CursorUtils.SELECTION_PHOTOS)
        }
    }

    private fun openAlbum(album: Album) {
        PickerMainActivity.mediaItems = album.mediaItems

        findNavController().navigate(
            R.id.action_albumPickerFragment_to_albumPickerInfoFragment,
            Bundle().apply {
                putString("albumNameKey", album.name)
            })
    }

}