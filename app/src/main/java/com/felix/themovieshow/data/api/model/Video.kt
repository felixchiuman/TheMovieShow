package com.felix.themovieshow.data.api.model

// Model untuk endpoint GET /movie/{movie_id}/videos

data class Video(
    val id: String,
    val key: String,
    val site: String,
    val type: String
)

data class VideoResponse(
    val results: List<Video>
)