package com.felix.themovieshow.data.api.model

import com.squareup.moshi.Json

// Model untuk endpoint GET /movie/{movie_id}/reviews

data class Review(
    val id: String,
    val author: String,
    val content: String,
    @Json(name = "author_details") val authorDetails: AuthorDetails?
)

data class AuthorDetails(
    val rating: Double?
)

data class ReviewPagedResponse(
    val page: Int,
    val results: List<Review>,
    @Json(name = "total_pages") val totalPages: Int
)