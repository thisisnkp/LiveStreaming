package com.comman.gallerypicker.Utils

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

object FileUtil {

    private const val FOLDER_PHOTO_EDIT = "GalleryPhotoEdit"
    private const val FOLDER_VIDEO_EDIT = "GalleryVideoEdit"
    const val FOLDER_PHOTO = "GalleryPhoto"
    const val FOLDER_VIDEO = "GalleryVideo"
    const val FOLDER_VAULT = ".PrivateLockGallery"
    const val FOLDER_DELETE = ".PrivateDeleteGallery"
    const val FOLDER_APP = "Gallery"

    fun getUnhideFolder(isVideo: Boolean): File {
        val file = if (isVideo) {
            File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MOVIES
                ).toString() + File.separator + FOLDER_VIDEO
            )
        } else {
            File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                ).toString() + File.separator + FOLDER_PHOTO
            )
        }
        if (!file.exists())
            file.mkdirs()
        return file
    }

    fun getVaultImagePath(): String {
        val file = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            ).toString() + File.separator + FOLDER_VAULT
        )
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.absolutePath
    }

    fun getVaultVideoPath(): String {
        val file = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
            ).toString() + File.separator + FOLDER_VAULT
        )
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.absolutePath
    }

    /**
     *  file name
     */
    fun getFileName(extension: String): String {
        return SimpleDateFormat(
            "EEE, dd MMM yyyy HH:mm:ss",
            Locale.getDefault()
        ).format(Calendar.getInstance().time) + extension
    }

    /***
     * pictures file
     */
    fun getPicturesFile(): File {
        val file = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            ).toString() + File.separator + FOLDER_PHOTO_EDIT
        )
        if (!file.exists()) {
            file.mkdirs()
        }
        return file
    }

    /**
     * videos file
     */
    fun getVideosFile(): File {
        val file = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
            ).toString() + File.separator + FOLDER_VIDEO_EDIT
        )
        if (!file.exists()) {
            file.mkdirs()
        }
        return file
    }

    /**
     * content values to save image into folder
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getPicturesContentValues(width: Int, height: Int) = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, getFileName(".jpg"))
        put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        put(MediaStore.Images.Media.MIME_TYPE, "image" + "/jpeg")
        put(MediaStore.Images.Media.WIDTH, width)
        put(MediaStore.Images.Media.HEIGHT, height)
        put(
            MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES + File.separator + FOLDER_PHOTO_EDIT
        )
        put(MediaStore.Images.Media.IS_PENDING, true)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getVideoContentValues() = ContentValues().apply {
        val name = getFileName(".mp4")
        put(MediaStore.Video.Media.TITLE, name)
        put(MediaStore.Video.Media.DISPLAY_NAME, name)
        put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        put(MediaStore.Video.Media.MIME_TYPE, "video" + "/mp4")
        put(
            MediaStore.Video.Media.RELATIVE_PATH,
            Environment.DIRECTORY_MOVIES + File.separator + FOLDER_VIDEO_EDIT
        )
        put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
        put(MediaStore.Video.Media.IS_PENDING, true)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getCopyFileContentValues(fileName: String, newPath: String, mimeType: String) =
        ContentValues().apply {
            put(MediaStore.Video.Media.TITLE, fileName)
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Video.Media.MIME_TYPE, mimeType)
            put(MediaStore.Video.Media.RELATIVE_PATH, newPath)
            put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
            put(MediaStore.Video.Media.IS_PENDING, true)
        }

    fun isAlbumPhotoDirectory(albumName: String): Boolean {
        val dirPhoto = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        ).toString() + File.separator + FOLDER_VAULT
        if (File(dirPhoto, albumName).exists())
            return true
        return false
    }

    fun isAlbumVideoDirectory(albumName: String): Boolean {
        val dirPhoto = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_MOVIES
        ).toString() + File.separator + FOLDER_VAULT
        if (File(dirPhoto, albumName).exists())
            return true
        return false
    }

    fun isAlbumDirectory(albumName: String): Boolean {
//        Environment.getStorageDirectory()
        val dirPhoto = Environment.getStorageDirectory().toString() + File.separator + FOLDER_VAULT
        if (File(dirPhoto, albumName).exists())
            return true
        return false
    }

    fun getAlBumDirectory(albumName: String): String {
        val dirPhoto = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        ).toString() + File.separator + FOLDER_VAULT
        val dirVideo = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_MOVIES
        ).toString() + File.separator + FOLDER_VAULT
        return if (File(dirPhoto, albumName).exists())
            dirPhoto
        else if (File(dirVideo, albumName).exists())
            dirVideo
        else
            ""
    }

    fun <T : Serializable?> getSerializable(intent: Intent, key: String, m_class: Class<T>): T {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getSerializableExtra(key, m_class)!!
        else
            intent.getSerializableExtra(key) as T
    }

}