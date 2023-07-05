package com.example.ytscrobblefilter

class Video(Title: String, Creator: String, Duration: Long) {
    val title = Title
    val creator = Creator
    val duration = Duration//In ms.

    /* All data I can get from YT controller:
    0 = "android.media.metadata.ALBUM_ART"
    1 = "android.media.metadata.ARTIST"
    2 = "com.google.android.youtube.MEDIA_METADATA_VIDEO_HEIGHT_PX"
    3 = "android.media.metadata.DURATION"
    4 = "com.google.android.youtube.MEDIA_METADATA_VIDEO_WIDTH_PX"
    5 = "android.media.metadata.TITLE"
    6 = "android.media.metadata.ALBUM_ARTIST"
    */
}