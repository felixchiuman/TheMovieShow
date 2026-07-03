package com.felix.themovieshow.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.felix.themovieshow.data.api.model.AuthorDetails
import com.felix.themovieshow.data.api.model.Review
import com.felix.themovieshow.data.api.model.ReviewPagedResponse
import com.felix.themovieshow.data.repository.ReviewRepository
import com.felix.themovieshow.data.resource.Resource
import com.felix.themovieshow.ui.review.ReviewListViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ReviewRepository
    private val movieId = 931285

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ReviewListViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("movieId" to movieId))
        return ReviewListViewModel(repository, savedStateHandle)
    }

    private fun review(id: String) = Review(id, "Author $id", "Content $id", AuthorDetails(8.0))

    // ============ Positive case: init() ============

    @Test
    fun `init loads first page of reviews on success`() = runTest(testDispatcher) {
        coEvery { repository.getMovieReviews(movieId, 1) } returns
            Resource.Success(ReviewPagedResponse(1, listOf(review("1"), review("2")), totalPages = 3))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.reviews.size)
        assertEquals(1, state.currentPage)
        assertFalse(state.endReached)
        assertFalse(state.isLoading)
    }

    // ============ Negative case: init() ============

    @Test
    fun `init sets errorMessage when api call fails`() = runTest(testDispatcher) {
        coEvery { repository.getMovieReviews(movieId, 1) } returns Resource.Error("Server error")

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Server error", state.errorMessage)
        assertTrue(state.reviews.isEmpty())
    }

    // ============ Pagination: loadMore() ============

    @Test
    fun `loadMore appends next page results to existing list`() = runTest(testDispatcher) {
        coEvery { repository.getMovieReviews(movieId, 1) } returns
            Resource.Success(ReviewPagedResponse(1, listOf(review("1")), totalPages = 3))
        coEvery { repository.getMovieReviews(movieId, 2) } returns
            Resource.Success(ReviewPagedResponse(2, listOf(review("2")), totalPages = 3))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("1", "2"), state.reviews.map { it.id })
        assertEquals(2, state.currentPage)
    }

    @Test
    fun `endReached becomes true when currentPage reaches totalPages`() = runTest(testDispatcher) {
        coEvery { repository.getMovieReviews(movieId, 1) } returns
            Resource.Success(ReviewPagedResponse(1, listOf(review("1")), totalPages = 2))
        coEvery { repository.getMovieReviews(movieId, 2) } returns
            Resource.Success(ReviewPagedResponse(2, listOf(review("2")), totalPages = 2))

        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.endReached)

        viewModel.loadMore()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.endReached)
    }

    @Test
    fun `loadMore does nothing once endReached is true`() = runTest(testDispatcher) {
        coEvery { repository.getMovieReviews(movieId, 1) } returns
            Resource.Success(ReviewPagedResponse(1, listOf(review("1")), totalPages = 1))

        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.endReached)

        viewModel.loadMore()
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.getMovieReviews(movieId, any()) }
    }

    @Test
    fun `loadMore does nothing while a fetch is already in progress`() = runTest(testDispatcher) {
        coEvery { repository.getMovieReviews(movieId, 1) } returns
            Resource.Success(ReviewPagedResponse(1, listOf(review("1")), totalPages = 5))
        coEvery { repository.getMovieReviews(movieId, 2) } returns
            Resource.Success(ReviewPagedResponse(2, listOf(review("2")), totalPages = 5))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadMore()
        // sengaja tidak advanceUntilIdle() dulu -- cek isLoading masih true di titik ini
        assertTrue(viewModel.uiState.value.isLoading)

        viewModel.loadMore() // harus di-ignore
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.getMovieReviews(movieId, 2) }
    }

    // ============ retry() ============

    @Test
    fun `retry resets to page 1 and refetches`() = runTest(testDispatcher) {
        coEvery { repository.getMovieReviews(movieId, 1) } returns Resource.Error("Server error")

        val viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.reviews.isEmpty())

        coEvery { repository.getMovieReviews(movieId, 1) } returns
            Resource.Success(ReviewPagedResponse(1, listOf(review("1")), totalPages = 3))

        viewModel.retry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.reviews.size)
        assertEquals(null, state.errorMessage)
    }
}
