package com.comman.gallerypicker.Utils

import android.content.Context
import android.text.format.Formatter
import java.util.concurrent.TimeUnit

/**
 * get formatted duration
 */
//fun Long.getDuration(): String = SimpleDateFormat(
//    "mm:ss",
//    Locale.getDefault()
//).format(Date(this))
fun Long.getDuration(): String = String.format(
    "%d:%d",
    TimeUnit.MILLISECONDS.toMinutes(this),
    TimeUnit.MILLISECONDS.toSeconds(this) -
            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this))
)

/**
 * get file size
 */
fun Long.getSize(context: Context): String = Formatter.formatFileSize(context, this)