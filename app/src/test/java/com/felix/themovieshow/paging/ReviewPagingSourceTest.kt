package com.felix.themovieshow.paging

import androidx.paging.PagingSource
import com.felix.themovieshow.data.api.model.Review
import com.felix.themovieshow.data.api.model.ReviewPagedResponse
import com.felix.themovieshow.data.api.network.ApiService
import com.felix.themovieshow.data.paging.ReviewPagingSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ReviewPagingSourceTest {

    private lateinit var api: ApiService
    private val movieId = 931285

    @Before
    fun setUp() {
        api = mockk()
    }

    private fun sampleReview(id: String) = Review(
        id = id,
        author = "Author $id",
        content = "Review $id",
        authorDetails = null
    )

    private fun refreshParams() = PagingSource.LoadParams.Refresh<Int>(
        key = null,
        loadSize = ReviewPagingSource.PAGE_SIZE,
        placeholdersEnabled = false
    )

    @Test
    fun `load returns first page with nextKey when more pages available`() = runTest {
        coEvery { api.getMovieReviews(movieId, 1) } returns ReviewPagedResponse(
            page = 1,
            results = listOf(sampleReview("1"), sampleReview("2")),
            totalPages = 3
        )

        val result = ReviewPagingSource(api, movieId).load(refreshParams())

        val page = result as PagingSource.LoadResult.Page
        assertEquals(listOf("1", "2"), page.data.map { it.id })
        assertNull(page.prevKey)
        assertEquals(2, page.nextKey)
    }

    @Test
    fun `load returns null nextKey when movie has no reviews`() = runTest {
        coEvery { api.getMovieReviews(movieId, 1) } returns ReviewPagedResponse(
            page = 1,
            results = emptyList(),
            totalPages = 1
        )

        val result = ReviewPagingSource(api, movieId).load(refreshParams())

        val page = result as PagingSource.LoadResult.Page
        assertTrue(page.data.isEmpty())
        assertNull(page.nextKey)
    }

    @Test
    fun `load returns Error when api throws exception`() = runTest {
        coEvery { api.getMovieReviews(movieId, 1) } throws IOException("Server error")

        val result = ReviewPagingSource(api, movieId).load(refreshParams())

        assertTrue(result is PagingSource.LoadResult.Error)
    }
}
