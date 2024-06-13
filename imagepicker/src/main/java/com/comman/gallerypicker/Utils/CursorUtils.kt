package com.comman.gallerypicker.Utils

import android.annotation.SuppressLint
import android.net.Uri
import android.provider.MediaStore

object CursorUtils {

    val CONTENT_URI: Uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)

    fun getMediaContentUri(mediaType: Int): Uri =
        if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI else
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    const val SORT_BY_DATE = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

    @SuppressLint("InlinedApi")
    val PROJECTION = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
        MediaStore.Files.FileColumns.DISPLAY_NAME,
        MediaStore.Files.FileColumns.DATA,
        MediaStore.Files.FileColumns.SIZE,
        MediaStore.Files.FileColumns.MEDIA_TYPE,
        MediaStore.Files.FileColumns.MIME_TYPE,
        MediaStore.Files.FileColumns.DURATION,
        MediaStore.Files.FileColumns.DATE_ADDED
    )

    const val SELECTION_ALL = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = " +
            "${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE} " +
            "OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = " +
            "${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO}"

    const val SELECTION_PHOTOS = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = " +
            "${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE} "

    const val SELECTION_VIDEOS = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = " +
            "${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO} "

    const val nonMediaCondition = (MediaStore.Files.FileColumns.MEDIA_TYPE
            + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE)
    const val where = (nonMediaCondition + " AND "
            + MediaStore.Files.FileColumns.DATA + " LIKE ?")

    var params = arrayOf("%${FileUtil.FOLDER_VAULT}/%")

    const val whereDelete =
        ("(" + SELECTION_ALL + ") AND " + MediaStore.Files.FileColumns.DATA + " LIKE ?")

    var paramsDelete = arrayOf("%${FileUtil.FOLDER_DELETE}/%")


}