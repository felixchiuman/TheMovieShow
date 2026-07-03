package com.felix.themovieshow.viewmodel

import com.felix.themovieshow.data.api.model.Genre
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.api.model.MoviePagedResponse
import com.felix.themovieshow.data.repository.HomeRepository
import com.felix.themovieshow.data.resource.Resource
import com.felix.themovieshow.ui.home.HomeViewModel
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
    fun `init loads genres and movies of first genre on success`() = runTest(testDispatcher) {
        val genres = listOf(Genre(1, "Action"), Genre(2, "Drama"))
        coEvery { repository.getGenres() } returns Resource.Success(genres)
        coEvery { repository.discoverMoviesByGenre(1, 1) } returns Resource.Success(
            MoviePagedResponse(page = 1, results = listOf(sampleMovie(1)), totalPages = 5)
        )

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(genres, state.genres)
        assertEquals(1, state.selectedGenreId)
        assertEquals(1, state.movies.size)
        assertFalse(state.isLoadingGenres)
        assertFalse(state.isLoadingMovies)
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
    fun `onGenreSelected does nothing when the same genre is already selected`() = runTest(testDispatcher) {
        val genres = listOf(Genre(1, "Action"))
        coEvery { repository.getGenres() } returns Resource.Success(genres)
        coEvery { repository.discoverMoviesByGenre(1, 1) } returns Resource.Success(
            MoviePagedResponse(1, listOf(sampleMovie(1)), 5)
        )

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        viewModel.onGenreSelected(Genre(1, "Action")) // genre yang sama seperti yang sudah dipilih
        advanceUntilIdle()

        // discoverMoviesByGenre(1, 1) cuma boleh kepanggil SEKALI (dari init), tidak nge-refetch
        coVerify(exactly = 1) { repository.discoverMoviesByGenre(1, 1) }
    }

    // ============ REGRESSION TEST ============

    @Test
    fun `calling loadMoreMovies three times rapidly only triggers ONE fetch for the next page`() =
        runTest(testDispatcher) {
            val genres = listOf(Genre(1, "Action"))
            coEvery { repository.getGenres() } returns Resource.Success(genres)
            coEvery { repository.discoverMoviesByGenre(1, 1) } returns Resource.Success(
                MoviePagedResponse(1, listOf(sampleMovie(1)), totalPages = 5)
            )
            coEvery { repository.discoverMoviesByGenre(1, 2) } returns Resource.Success(
                MoviePagedResponse(2, listOf(sampleMovie(2)), totalPages = 5)
            )

            val viewModel = HomeViewModel(repository)
            advanceUntilIdle()

            viewModel.loadMoreMovies()
            viewModel.loadMoreMovies()
            viewModel.loadMoreMovies()
            advanceUntilIdle()

            coVerify(exactly = 1) { repository.discoverMoviesByGenre(1, 2) }
        }

    @Test
    fun `loadMoreMovies does nothing when a fetch is already in progress`() = runTest(testDispatcher) {
        val genres = listOf(Genre(1, "Action"))
        coEvery { repository.getGenres() } returns Resource.Success(genres)
        coEvery { repository.discoverMoviesByGenre(1, 1) } returns Resource.Success(
            MoviePagedResponse(1, listOf(sampleMovie(1)), totalPages = 5)
        )
        coEvery { repository.discoverMoviesByGenre(1, 2) } returns Resource.Success(
            MoviePagedResponse(2, listOf(sampleMovie(2)), totalPages = 5)
        )

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        viewModel.loadMoreMovies()
        assertTrue(viewModel.uiState.value.isLoadingMovies)

        viewModel.loadMoreMovies()
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.discoverMoviesByGenre(1, 2) }
    }

    @Test
    fun `loadMoreMovies does nothing when no genre is selected`() = runTest(testDispatcher) {
        coEvery { repository.getGenres() } returns Resource.Success(emptyList())

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        viewModel.loadMoreMovies()
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.discoverMoviesByGenre(any(), any()) }
    }

    // ============ distinctBy safety net ============

    @Test
    fun `merging paginated results removes duplicate movie ids`() = runTest(testDispatcher) {
        val genres = listOf(Genre(1, "Action"))
        coEvery { repository.getGenres() } returns Resource.Success(genres)
        coEvery { repository.discoverMoviesByGenre(1, 1) } returns Resource.Success(
            MoviePagedResponse(1, listOf(sampleMovie(1)), totalPages = 5)
        )

        coEvery { repository.discoverMoviesByGenre(1, 2) } returns Resource.Success(
            MoviePagedResponse(2, listOf(sampleMovie(1), sampleMovie(2)), totalPages = 5)
        )

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        viewModel.loadMoreMovies()
        advanceUntilIdle()

        val movieIds = viewModel.uiState.value.movies.map { it.id }
        assertEquals(listOf(1, 2), movieIds)
        assertEquals(movieIds.size, movieIds.distinct().size)
    }
}
