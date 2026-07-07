package com.felix.themovieshow.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.felix.themovieshow.data.api.model.Review
import com.felix.themovieshow.data.repository.ReviewRepository
import com.felix.themovieshow.ui.review.ReviewListViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Page tracking, endReached, dan error handling ditest di
 * [com.felix.themovieshow.paging.ReviewPagingSourceTest].
 */
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

    private fun sampleReview(id: String) = Review(
        id = id,
        author = "Author $id",
        content = "Review $id",
        authorDetails = null
    )

    @Test
    fun `reviews emits paging data from repository`() = runTest(testDispatcher) {
        every { repository.getMovieReviewsPaged(movieId) } returns flowOf(
            PagingData.from(listOf(sampleReview("1"), sampleReview("2")))
        )

        val snapshot = createViewModel().reviews.asSnapshot()

        assertEquals(listOf("1", "2"), snapshot.map { it.id })
    }

    @Test
    fun `duplicate reviews from api are filtered out`() = runTest(testDispatcher) {
        every { repository.getMovieReviewsPaged(movieId) } returns flowOf(
            PagingData.from(listOf(sampleReview("1"), sampleReview("2"), sampleReview("1")))
        )

        val snapshot = createViewModel().reviews.asSnapshot()

        assertEquals(listOf("1", "2"), snapshot.map { it.id })
    }

    @Test
    fun `reviews emits empty list when movie has no reviews`() = runTest(testDispatcher) {
        every { repository.getMovieReviewsPaged(movieId) } returns flowOf(
            PagingData.from(emptyList())
        )

        val snapshot = createViewModel().reviews.asSnapshot()

        assertEquals(emptyList<Review>(), snapshot)
    }
}
