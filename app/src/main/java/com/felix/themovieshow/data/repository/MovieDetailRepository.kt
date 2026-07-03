package com.felix.themovieshow.data.repository

import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.api.model.Video
import com.felix.themovieshow.data.api.network.ApiService
import com.felix.themovieshow.data.resource.Resource
import javax.inject.Inject
import javax.inject.Singleton


interface MovieDetailRepository {
    suspend fun getMovieDetail(movieId: Int): Resource<Movie>
    suspend fun getMovieTrailer(movieId: Int): Resource<Video?>
}

@Singleton
class MovieDetailRepositoryImpl @Inject constructor(
    private val api: ApiService
) : MovieDetailRepository {

    override suspend fun getMovieDetail(movieId: Int): Resource<Movie> {
        return try {
            val movie = api.getMovieDetail(movieId)
            Resource.Success(movie)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal mengambil detail film")
        }
    }

    override suspend fun getMovieTrailer(movieId: Int): Resource<Video?> {
        return try {
            val response = api.getMovieVideos(movieId)
            val trailer = response.results.firstOrNull { it.site == "YouTube" && it.type == "Trailer" }
            Resource.Success(trailer)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal mengambil trailer")
        }
    }
}