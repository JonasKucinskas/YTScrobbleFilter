package com.example.ytscrobblefilter

import android.graphics.Bitmap
import android.media.MediaMetadata

class Track(metadata: MediaMetadata) {
    var cover: Bitmap? = metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
    val duration: Long = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
    var title: String = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
    var artist: String? = null

}