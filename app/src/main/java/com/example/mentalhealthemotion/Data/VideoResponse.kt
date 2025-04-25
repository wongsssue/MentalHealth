package com.example.mentalhealthemotion.Data

data class VideoResponse(
    val items: List<VideoItem>
)

data class VideoItem(
    val id: VideoId,
    val snippet: Snippet,
    val category: String
)

data class VideoId(
    val videoId: String
)

data class Snippet(
    val title: String,
    val description: String,
    val thumbnails: Thumbnails,
    val channelTitle: String
)

data class Thumbnails(
    val medium: Thumbnail
)

data class Thumbnail(
    val url: String
)
