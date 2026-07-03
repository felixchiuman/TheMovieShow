package com.felix.themovieshow.data.api.network

import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.api.model.PopularMovieResponse
import com.felix.themovieshow.data.api.model.ReviewPagedResponse
import com.felix.themovieshow.data.api.model.TopRatedMovieResponse
import com.felix.themovieshow.data.api.model.VideoResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // User story 3: primary info saat movie diklik
    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String = "en-US"
    ): Movie

    // User story 4: reviews (dengan pagination -> endless scrolling)
    @GET("movie/{movie_id}/reviews")
    suspend fun getMovieReviews(
        @Path("movie_id") movieId: Int,
        @Query("page") page: Int
    ): ReviewPagedResponse

    // User story 5: trailer youtube (filter site == "YouTube" & type == "Trailer" di repository)
    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int
    ): VideoResponse

    // Parkee story 1: popular list
    @GET("movie/popular")
    suspend fun getMoviePopular(
        @Query("page") page: Int,
        @Query("language") language: String = "en-US"
    ): PopularMovieResponse

    // Parkee story 2: top rated
    @GET("movie/top_rated")
    suspend fun getTopRated(
        @Query("page") page: Int,
        @Query("language") language: String = "en-US"
    ): TopRatedMovieResponse
}