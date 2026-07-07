package com.felix.themovieshow.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.felix.themovieshow.data.api.model.Review
import com.felix.themovieshow.data.api.model.ReviewPagedResponse
import com.felix.themovieshow.data.api.network.ApiService
import com.felix.themovieshow.data.paging.ReviewPagingSource
import com.felix.themovieshow.data.resource.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface ReviewRepository {
    /** Masih dipakai MovieDetailViewModel untuk preview review (page 1 saja). */
    suspend fun getMovieReviews(movieId: Int, page: Int): Resource<ReviewPagedResponse>

    /** Paging 3 untuk endless scrolling di ReviewListScreen. */
    fun getMovieReviewsPaged(movieId: Int): Flow<PagingData<Review>>
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

    override fun getMovieReviewsPaged(movieId: Int): Flow<PagingData<Review>> =
        Pager(
            config = PagingConfig(
                pageSize = ReviewPagingSource.PAGE_SIZE,
                initialLoadSize = ReviewPagingSource.PAGE_SIZE,
                prefetchDistance = 3, // sama dengan trigger manual sebelumnya (index >= size - 3)
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ReviewPagingSource(api, movieId) }
        ).flow
}
