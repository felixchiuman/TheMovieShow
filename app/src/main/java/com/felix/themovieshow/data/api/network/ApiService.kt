package com.felix.themovieshow.data.api.network

import com.felix.themovieshow.data.api.model.GenreResponse
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.api.model.MoviePagedResponse
import com.felix.themovieshow.data.api.model.ReviewPagedResponse
import com.felix.themovieshow.data.api.model.VideoResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // User story 1: list of official genres
    @GET("genre/movie/list")
    suspend fun getGenres(
        @Query("language") language: String = "en-US"
    ): GenreResponse

    // User story 2: discover movies by genre (dengan pagination -> endless scrolling)
    @GET("discover/movie")
    suspend fun discoverMoviesByGenre(
        @Query("with_genres") genreId: Int,
        @Query("page") page: Int,
        @Query("language") language: String = "en-US"
    ): MoviePagedResponse

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
}