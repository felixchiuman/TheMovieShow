package com.felix.themovieshow.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.repository.HomeRepository
import com.felix.themovieshow.ui.viewmore.ViewMoreViewModel
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
 * Setelah migrasi ke Paging 3, page tracking, endReached, dan error handling
 * ditest di [com.felix.themovieshow.paging.MoviePagingSourceTest].
 * Test di sini fokus ke logika yang tersisa di ViewModel: dedup filter.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ViewMoreViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: HomeRepository
    private val genreId = 1

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ViewMoreViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("genreId" to genreId))
        return ViewMoreViewModel(repository, savedStateHandle)
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

    // ============ POSITIVE CASE ============

    @Test
    fun `movies emits paging data from repository`() = runTest(testDispatcher) {
        every { repository.getMoviesByGenrePaged(genreId) } returns flowOf(
            PagingData.from(listOf(sampleMovie(1), sampleMovie(2)))
        )

        val snapshot = createViewModel().movies.asSnapshot()

        assertEquals(listOf(1, 2), snapshot.map { it.id })
    }

    // ============ DEDUP (pengganti distinctBy lama) ============

    @Test
    fun `duplicate movies from api are filtered out`() = runTest(testDispatcher) {
        // Simulasi TMDB return movie yang sama dua kali (dulu bikin crash
        // duplicate key di LazyVerticalGrid sebelum ada distinctBy)
        every { repository.getMoviesByGenrePaged(genreId) } returns flowOf(
            PagingData.from(listOf(sampleMovie(1), sampleMovie(2), sampleMovie(1)))
        )

        val snapshot = createViewModel().movies.asSnapshot()

        assertEquals(listOf(1, 2), snapshot.map { it.id })
    }

    @Test
    fun `movies emits empty list when repository has no data`() = runTest(testDispatcher) {
        every { repository.getMoviesByGenrePaged(genreId) } returns flowOf(
            PagingData.from(emptyList())
        )

        val snapshot = createViewModel().movies.asSnapshot()

        assertEquals(emptyList<Movie>(), snapshot)
    }
}
