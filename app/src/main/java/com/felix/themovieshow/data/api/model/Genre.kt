package com.felix.themovieshow.data.api.model

// Model untuk endpoint GET /genre/movie/list

data class Genre(
    val id: Int,
    val name: String
)

data class GenreResponse(
    val genres: List<Genre>
)