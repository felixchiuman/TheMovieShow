package com.felix.themovieshow.viewmodel

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.felix.themovieshow.data.api.model.Genre
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.repository.HomeRepository
import com.felix.themovieshow.data.resource.Resource
import com.felix.themovieshow.ui.home.HomeViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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

/**
 * Race condition loadMoreMovies (double fetch saat spam load more) sekarang
 * ditangani Paging 3 by design -- regression test lamanya tersimpan di git
 * history. Page tracking & error handling ditest di MoviePagingSourceTest.
 * Test di sini fokus ke: state genre, switching genre (flatMapLatest), dan
 * dedup filter.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: HomeRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun sampleMovie(id: Int) = Movie(
        id = id,
        title = "Movie $id",
        posterPath = null,
        backdropPath = null,
        overview = "",
        releaseDate = "2026-01-01",
        voteAverage = 7.0,
        genreIds = listOf(1)
    )

    // ============ Positive case: init() ============

    @Test
    fun `init loads genres and selects first genre on success`() = runTest(testDispatcher) {
        val genres = listOf(Genre(1, "Action"), Genre(2, "Drama"))
        coEvery { repository.getGenres() } returns Resource.Success(genres)
        every { repository.getMoviesByGenrePaged(1) } returns flowOf(
            PagingData.from(listOf(sampleMovie(1)))
        )

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(genres, state.genres)
        assertEquals(1, state.selectedGenreId)
        assertFalse(state.isLoadingGenres)

        val snapshot = viewModel.movies.asSnapshot()
        assertEquals(listOf(1), snapshot.map { it.id })
    }

    // ============ Negative case: init() ============

    @Test
    fun `init sets errorMessage when getGenres fails`() = runTest(testDispatcher) {
        coEvery { repository.getGenres() } returns Resource.Error("Network error")

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Network error", state.errorMessage)
        assertTrue(state.genres.isEmpty())
        assertFalse(state.isLoadingGenres)
    }

    // ============ onGenreSelected ============

    @Test
    fun `onGenreSelected switches movies to the new genre`() = runTest(testDispatcher) {
        val genres = listOf(Genre(1, "Action"), Genre(2, "Drama"))
        coEvery { repository.getGenres() } returns Resource.Success(genres)
        every { repository.getMoviesByGenrePaged(1) } returns flowOf(
            PagingData.from(listOf(sampleMovie(1)))
        )
        every { repository.getMoviesByGenrePaged(2) } returns flowOf(
            PagingData.from(listOf(sampleMovie(2)))
        )

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        viewModel.onGenreSelected(Genre(2, "Drama"))
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.selectedGenreId)
        // flatMapLatest cancel Pager genre lama, ganti dengan genre baru
        val snapshot = viewModel.movies.asSnapshot()
        assertEquals(listOf(2), snapshot.map { it.id })
    }

    @Test
    fun `onGenreSelected does nothing when the same genre is already selected`() = runTest(testDispatcher) {
        val genres = listOf(Genre(1, "Action"))
        coEvery { repository.getGenres() } returns Resource.Success(genres)
        every { repository.getMoviesByGenrePaged(1) } returns flowOf(
            PagingData.from(listOf(sampleMovie(1)))
        )

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        viewModel.onGenreSelected(Genre(1, "Action")) // genre yang sama
        advanceUntilIdle()

        viewModel.movies.asSnapshot()

        // Pager tidak boleh dibuat ulang untuk genre yang sama
        verify(exactly = 1) { repository.getMoviesByGenrePaged(1) }
    }

    // ============ Dedup safety net (pengganti distinctBy lama) ============

    @Test
    fun `duplicate movies from api are filtered out`() = runTest(testDispatcher) {
        val genres = listOf(Genre(1, "Action"))
        coEvery { repository.getGenres() } returns Resource.Success(genres)
        every { repository.getMoviesByGenrePaged(1) } returns flowOf(
            PagingData.from(listOf(sampleMovie(1), sampleMovie(1), sampleMovie(2)))
        )

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        val movieIds = viewModel.movies.asSnapshot().map { it.id }
        assertEquals(listOf(1, 2), movieIds)
        assertEquals(movieIds.size, movieIds.distinct().size)
    }
}
