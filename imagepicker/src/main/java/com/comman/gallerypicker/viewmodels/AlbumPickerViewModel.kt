package com.comman.gallerypicker.viewmodels

import android.app.Application
import android.content.ContentUris
import android.database.ContentObserver
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.comman.gallerypicker.Utils.CursorUtils
import com.comman.gallerypicker.Utils.registerMediaObserver
import com.comman.gallerypicker.models.Album
import com.comman.gallerypicker.models.MediaItemObj
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlbumPickerViewModel(
    application: Application
) : AndroidViewModel(application) {

    private var contentObserver: ContentObserver? = null

    //items for Albums fragment
    private val albumsMLD = MutableLiveData<List<Album>>()
    val albums: LiveData<List<Album>> = albumsMLD

    //items for Library fragment
    private val mediaItemsMLD = MutableLiveData<List<MediaItemObj>>()
    val mediaItems: LiveData<List<MediaItemObj>> = mediaItemsMLD

    private val mediaFilterMLD = MutableLiveData<Boolean>()
    val mediaFilter: LiveData<Boolean> = mediaFilterMLD

    private val forYouItemsMLD = MutableLiveData<List<Album>>()
    val forYouItems: LiveData<List<Album>> = forYouItemsMLD

    private val photoPickerMLD = MutableLiveData<List<Album>>()
    val photoPickerItems: LiveData<List<Album>> = photoPickerMLD

    fun loadAlbums(mediaItems: List<MediaItemObj> = mutableListOf()) {
        val albums = mutableListOf<Album>()
        var isFolder = false
        var index = 0

        mediaItems.forEach { mediaItem ->

            val folderName = mediaItem.folderName ?: "Unknown"

            for (i in 0 until albums.size) {
                if (albums[i].name == folderName) {
                    isFolder = true
                    index = i
                    break
                } else {
                    isFolder = false
                }
            }

            if (isFolder) {
                //get album from folder
                albums[index].mediaItems
                    .toMutableList()
                    .apply {
                        //add new media item
                        add(mediaItem)
                        //add list to album
                        albums[index].mediaItems = this
                    }
            } else {
                //create empty list
                mutableListOf<MediaItemObj>()
                    .apply {
                        //add new media item
                        add(mediaItem)
                        //create new album
                        Album(
                            folderName, mediaItem.id, this
                        ).apply {
                            //add album to albums
                            albums.add(this)
                        }
                    }
            }

        }
        albumsMLD.postValue(albums)
    }

    fun loadMediaItems(selection: String) {
        viewModelScope.launch {
            performLoadMediaItems(selection)
            if (contentObserver == null) {
                contentObserver = getApp().contentResolver
                    .registerMediaObserver {
                        loadMediaItems(selection)
                    }
            }
        }
    }

    fun loadDateWise(items: List<MediaItemObj> = mutableListOf()) {
        items.shuffled().let { it ->
            items.also { newList ->
                groupMediaItems(newList)
            }
        }
    }

    fun loadDateWisePhotoPicker(items: List<MediaItemObj> = mutableListOf()) {
        items.shuffled().let { it ->
            items.also { newList ->
                groupMediaPhotoItems(newList)
            }
        }
    }

    private suspend fun performLoadMediaItems(selection: String) = withContext(Dispatchers.IO) {
        try {
            val mediaItems = mutableListOf<MediaItemObj>()

            getApp().contentResolver.query(
                CursorUtils.CONTENT_URI,
                CursorUtils.PROJECTION,
                selection,
                null,
                CursorUtils.SORT_BY_DATE
            )?.use { cursor ->

                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val folderNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
                val nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val pathColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val mediaTypeColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
                val mimeTypeColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                val durationColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DURATION)
                val dateAddedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val folderName = cursor.getString(folderNameColumn)
                    val nameRaw = cursor.getString(nameColumn)
                    val pathRaw = cursor.getString(pathColumn)
                    val size = cursor.getLong(sizeColumn)
                    val mediaType = cursor.getInt(mediaTypeColumn)
                    val uri = ContentUris.withAppendedId(
                        CursorUtils.getMediaContentUri(mediaType), id
                    )
                    val mimeType = cursor.getString(mimeTypeColumn)
                    val duration = cursor.getLong(durationColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)

                    mediaItems += MediaItemObj(
                        id,
                        uri,
                        nameRaw,
                        pathRaw,
                        folderName,
                        size,
                        mediaType,
                        mimeType,
                        duration,
                        dateAdded
                    )

                }
            }
            withContext(Dispatchers.Main) {
                mediaItemsMLD.postValue(mediaItems)
                mediaFilterMLD.postValue(true)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                e.message
                mediaFilterMLD.postValue(false)
            }
        }
    }

    private fun groupMediaItems(items: List<MediaItemObj>) {
        items.sortedByDescending { it.dateAdded }
            .groupBy { it.getDayMonthYear() }
            .entries.map { Album(it.key, 1L, it.value) }
            .also { forYouItemsMLD.postValue(it) }
    }

    private fun groupMediaPhotoItems(items: List<MediaItemObj>) {

        val itemList = arrayListOf<MediaItemObj>()

        items.sortedByDescending { it.dateAdded }
            .groupBy { it.getDayMonthYear() }.entries.map {
                Album(it.key, 1L, it.value)
            }.also {
                photoPickerMLD.postValue(it)
            }
    }

    override fun onCleared() {
        super.onCleared()
        contentObserver?.let {
            getApp().contentResolver.unregisterContentObserver(it)
        }
    }

    /**
     * get app context
     */
    private fun getApp() = getApplication<Application>()

}