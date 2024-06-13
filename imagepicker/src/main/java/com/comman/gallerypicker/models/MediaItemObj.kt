package com.comman.gallerypicker.models

import android.net.Uri
import android.os.Environment
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//key to share-get media
const val MEDIA_KEY = "MediaKey"

data class MediaItemObj(
    var id: Long = 0L,
    var uri: Uri? = Uri.EMPTY,
    var nameRaw: String? = null,
    var pathRaw: String? = null,
    var folderName: String? = null,
    var size: Long = 0L,
    var mediaType: Int = MediaStore.Files.FileColumns.MEDIA_TYPE_NONE,
    var mimeType: String? = "",
    //0 = no duration
    var duration: Long = 0L,
    var dateAdded: Long = 0L,
    //to order media in favorites fragment
    var timestampFavorite: Long = 0L
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readParcelable(Uri::class.java.classLoader),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeParcelable(uri, flags)
        parcel.writeString(nameRaw)
        parcel.writeString(pathRaw)
        parcel.writeString(folderName)
        parcel.writeLong(size)
        parcel.writeInt(mediaType)
        parcel.writeString(mimeType)
        parcel.writeLong(duration)
        parcel.writeLong(dateAdded)
        parcel.writeLong(timestampFavorite)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MediaItemObj> {
        override fun createFromParcel(parcel: Parcel): MediaItemObj {
            return MediaItemObj(parcel)
        }

        override fun newArray(size: Int): Array<MediaItemObj?> {
            return arrayOfNulls(size)
        }
    }

    fun isVideo() = mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO

    fun isVaultVideo() = pathRaw.toString().contains(Environment.DIRECTORY_MOVIES)

    fun getDayMonthYear(): String = getDate("dd MMMM yyy")

    fun getMonthYear(): String = getDate("MMMM yyy")

    fun getYear(): String = getDate("yyy")

    private fun getDate(pattern: String): String =
        SimpleDateFormat(
            pattern, Locale.getDefault()
        ).format(Date(dateAdded * 1000L))

    //remove extension .mp4 .jpg
    var name = nameRaw?.substringBeforeLast(".") ?: "Unknown"

}