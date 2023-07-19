package com.example.ytscrobblefilter

import android.graphics.Bitmap
import android.media.MediaMetadata

class Track(metadata: MediaMetadata) {
    val cover: Bitmap = metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
    val artist: String = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
    val duration: Long = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
    val title: String = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
    val albumArtist: String = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
}