package com.felix.themovieshow.data.api.model

import com.squareup.moshi.Json

// Model untuk endpoint GET /discover/movie dan GET /movie/{movie_id}

data class Movie(
    val id: Int,
    val title: String,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "backdrop_path") val backdropPath: String?,
    val overview: String,
    @Json(name = "release_date") val releaseDate: String?,
    @Json(name = "vote_average") val voteAverage: Double,
    @Json(name = "genre_ids") val genreIds: List<Int> = emptyList()
) {
    val posterUrl: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }

    val backdropUrl: String?
        get() = backdropPath?.let { "https://image.tmdb.org/t/p/w780$it" }
}

data class MoviePagedResponse(
    val page: Int,
    val results: List<Movie>,
    @Json(name = "total_pages") val totalPages: Int
)