package com.felix.themovieshow.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.api.network.ApiService

/**
 * PagingSource untuk GET /discover/movie.
 *
 * Menggantikan manual paging (currentPage + endReached + isLoading guard) yang
 * sebelumnya ada di ViewModel. Paging 3 yang handle:
 * - page tracking (params.key)
 * - end of pagination (nextKey == null, dulu flag endReached)
 * - concurrent load guard (dulu sumber race condition di loadMore)
 */
class MoviePagingSource(
    private val api: ApiService,
    private val genreId: Int
) : PagingSource<Int, Movie>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: STARTING_PAGE
        return try {
            val response = api.discoverMoviesByGenre(genreId, page)
            LoadResult.Page(
                data = response.results,
                prevKey = if (page == STARTING_PAGE) null else page - 1,
                nextKey = if (page >= response.totalPages || response.results.isEmpty()) {
                    null // end reached
                } else {
                    page + 1
                }
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        // Saat refresh, mulai dari page yang paling dekat dengan posisi scroll terakhir
        return state.anchorPosition?.let { anchor ->
            val closestPage = state.closestPageToPosition(anchor)
            closestPage?.prevKey?.plus(1) ?: closestPage?.nextKey?.minus(1)
        }
    }

    companion object {
        const val STARTING_PAGE = 1

        /** TMDB selalu return 20 item per page dan tidak bisa dikonfigurasi. */
        const val PAGE_SIZE = 20
    }
}
