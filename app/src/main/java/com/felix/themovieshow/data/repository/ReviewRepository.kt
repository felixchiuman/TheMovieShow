package com.felix.themovieshow.data.repository

import com.felix.themovieshow.data.api.model.ReviewPagedResponse
import com.felix.themovieshow.data.api.network.ApiService
import com.felix.themovieshow.data.resource.Resource
import javax.inject.Inject
import javax.inject.Singleton

interface ReviewRepository {
    suspend fun getMovieReviews(movieId: Int, page: Int): Resource<ReviewPagedResponse>
}

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val api: ApiService
) : ReviewRepository {

    override suspend fun getMovieReviews(movieId: Int, page: Int): Resource<ReviewPagedResponse> {
        return try {
            val response = api.getMovieReviews(movieId, page)
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal mengambil review film")
        }
    }
}
