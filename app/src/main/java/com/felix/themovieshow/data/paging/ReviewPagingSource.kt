package com.felix.themovieshow.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.felix.themovieshow.data.api.model.Review
import com.felix.themovieshow.data.api.network.ApiService

/**
 * PagingSource untuk GET /movie/{movie_id}/reviews.
 * Pola sama persis dengan [MoviePagingSource], hanya beda endpoint dan model.
 */
class ReviewPagingSource(
    private val api: ApiService,
    private val movieId: Int
) : PagingSource<Int, Review>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Review> {
        val page = params.key ?: STARTING_PAGE
        return try {
            val response = api.getMovieReviews(movieId, page)
            LoadResult.Page(
                data = response.results,
                prevKey = if (page == STARTING_PAGE) null else page - 1,
                nextKey = if (page >= response.totalPages || response.results.isEmpty()) {
                    null
                } else {
                    page + 1
                }
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Review>): Int? {
        return state.anchorPosition?.let { anchor ->
            val closestPage = state.closestPageToPosition(anchor)
            closestPage?.prevKey?.plus(1) ?: closestPage?.nextKey?.minus(1)
        }
    }

    companion object {
        const val STARTING_PAGE = 1
        const val PAGE_SIZE = 20
    }
}
