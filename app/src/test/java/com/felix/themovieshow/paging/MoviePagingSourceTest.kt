package com.felix.themovieshow.paging

import androidx.paging.PagingSource
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.api.model.MoviePagedResponse
import com.felix.themovieshow.data.api.network.ApiService
import com.felix.themovieshow.data.paging.MoviePagingSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class MoviePagingSourceTest {

    private lateinit var api: ApiService
    private val genreId = 1

    @Before
    fun setUp() {
        api = mockk()
    }

    private fun sampleMovie(id: Int) = Movie(
        id = id,
        title = "Movie $id",
        posterPath = null,
        backdropPath = null,
        overview = "",
        releaseDate = "2026-01-01",
        voteAverage = 7.0,
        genreIds = listOf(genreId)
    )

    private fun refreshParams() = PagingSource.LoadParams.Refresh<Int>(
        key = null,
        loadSize = MoviePagingSource.PAGE_SIZE,
        placeholdersEnabled = false
    )

    // ============ POSITIVE CASE ============

    @Test
    fun `load returns first page with nextKey when more pages available`() = runTest {
        coEvery { api.discoverMoviesByGenre(genreId, 1) } returns MoviePagedResponse(
            page = 1,
            results = listOf(sampleMovie(1), sampleMovie(2)),
            totalPages = 5
        )

        val result = MoviePagingSource(api, genreId).load(refreshParams())

        val page = result as PagingSource.LoadResult.Page
        assertEquals(listOf(1, 2), page.data.map { it.id })
        assertNull(page.prevKey) // page 1 tidak punya prevKey
        assertEquals(2, page.nextKey)
    }

    @Test
    fun `load page 2 returns correct prevKey and nextKey`() = runTest {
        coEvery { api.discoverMoviesByGenre(genreId, 2) } returns MoviePagedResponse(
            page = 2,
            results = listOf(sampleMovie(3)),
            totalPages = 5
        )

        val result = MoviePagingSource(api, genreId).load(
            PagingSource.LoadParams.Append(
                key = 2,
                loadSize = MoviePagingSource.PAGE_SIZE,
                placeholdersEnabled = false
            )
        )

        val page = result as PagingSource.LoadResult.Page
        assertEquals(1, page.prevKey)
        assertEquals(3, page.nextKey)
    }

    // ============ END REACHED (pengganti flag endReached lama) ============

    @Test
    fun `load returns null nextKey on last page`() = runTest {
        coEvery { api.discoverMoviesByGenre(genreId, 1) } returns MoviePagedResponse(
            page = 1,
            results = listOf(sampleMovie(1)),
            totalPages = 1
        )

        val result = MoviePagingSource(api, genreId).load(refreshParams())

        val page = result as PagingSource.LoadResult.Page
        assertNull(page.nextKey) // Paging tidak akan request page 2
    }

    @Test
    fun `load returns null nextKey when results are empty`() = runTest {
        coEvery { api.discoverMoviesByGenre(genreId, 1) } returns MoviePagedResponse(
            page = 1,
            results = emptyList(),
            totalPages = 5
        )

        val result = MoviePagingSource(api, genreId).load(refreshParams())

        val page = result as PagingSource.LoadResult.Page
        assertTrue(page.data.isEmpty())
        assertNull(page.nextKey)
    }

    // ============ NEGATIVE CASE ============

    @Test
    fun `load returns Error when api throws exception`() = runTest {
        coEvery { api.discoverMoviesByGenre(genreId, 1) } throws IOException("Server error")

        val result = MoviePagingSource(api, genreId).load(refreshParams())

        assertTrue(result is PagingSource.LoadResult.Error)
        assertEquals("Server error", (result as PagingSource.LoadResult.Error).throwable.message)
    }
}
