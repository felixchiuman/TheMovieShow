package com.felix.themovieshow.repository

import com.felix.themovieshow.data.api.model.Review
import com.felix.themovieshow.data.api.model.ReviewPagedResponse
import com.felix.themovieshow.data.api.network.ApiService
import com.felix.themovieshow.data.repository.ReviewRepositoryImpl
import com.felix.themovieshow.data.resource.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ReviewRepositoryImplTest {

    private lateinit var api: ApiService
    private lateinit var repository: ReviewRepositoryImpl

    @Before
    fun setUp() {
        api = mockk()
        repository = ReviewRepositoryImpl(api)
    }

    @Test
    fun `getMovieReviews returns Success when api call succeeds`() = runTest {
        val fakeReview = Review(
            id = "1",
            author = "Felix",
            content = "Filmnya bagus banget",
            authorDetails = null
        )
        val fakeResponse = ReviewPagedResponse(page = 1, results = listOf(fakeReview), totalPages = 3)
        coEvery { api.getMovieReviews(931285, 1) } returns fakeResponse

        val result = repository.getMovieReviews(931285, 1)

        assertTrue(result is Resource.Success)
        assertEquals(fakeResponse, (result as Resource.Success).data)
    }

    @Test
    fun `getMovieReviews returns Success with empty list when movie has no reviews`() = runTest {
        val emptyResponse = ReviewPagedResponse(page = 1, results = emptyList(), totalPages = 1)
        coEvery { api.getMovieReviews(931285, 1) } returns emptyResponse

        val result = repository.getMovieReviews(931285, 1)

        assertTrue(result is Resource.Success)
        assertTrue((result as Resource.Success).data.results.isEmpty())
    }

    @Test
    fun `getMovieReviews returns Error when api call throws exception`() = runTest {
        coEvery { api.getMovieReviews(931285, 1) } throws IOException("Server error")

        val result = repository.getMovieReviews(931285, 1)

        assertTrue(result is Resource.Error)
        assertEquals("Server error", (result as Resource.Error).message)
    }
}
