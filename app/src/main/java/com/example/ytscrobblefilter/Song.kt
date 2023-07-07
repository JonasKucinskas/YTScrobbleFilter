package com.example.ytscrobblefilter

import android.media.MediaMetadata

class Song(metadata: MediaMetadata) {
    val cover = metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
    val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
    val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
    val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
    val albumArtist = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
}