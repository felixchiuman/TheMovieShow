package com.felix.themovieshow.repository

import com.felix.themovieshow.data.api.model.Genre
import com.felix.themovieshow.data.api.model.GenreResponse
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.api.model.MoviePagedResponse
import com.felix.themovieshow.data.api.network.ApiService
import com.felix.themovieshow.data.repository.HomeRepositoryImpl
import com.felix.themovieshow.data.resource.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class HomeRepositoryImplTest {
    private lateinit var api: ApiService
    private lateinit var repository: HomeRepositoryImpl

    @Before
    fun setUp() {
        api = mockk()
        repository = HomeRepositoryImpl(api)
    }

    @Test
    fun `getGenres returns Success when api call succeeds`() = runTest {
        val fakeGenres = listOf(Genre(id = 1, name = "Action"), Genre(id = 2, name = "Drama"))
        coEvery { api.getGenres() } returns GenreResponse(genres = fakeGenres)

        // Act
        val result = repository.getGenres()

        // Assert
        assertTrue(result is Resource.Success)
        assertEquals(fakeGenres, (result as Resource.Success).data)
    }

    @Test
    fun `getGenres returns Error when api call throws exception`() = runTest {
        coEvery { api.getGenres() } throws IOException("No internet connection")

        // Act
        val result = repository.getGenres()

        // Assert
        assertTrue(result is Resource.Error)
        assertEquals("No internet connection", (result as Resource.Error).message)
    }

    @Test
    fun `getGenres returns fallback message when exception has no message`() = runTest {
        coEvery { api.getGenres() } throws RuntimeException()

        val result = repository.getGenres()

        assertTrue(result is Resource.Error)
        assertEquals("Gagal mengambil daftar genre", (result as Resource.Error).message)
    }

    @Test
    fun `discoverMoviesByGenre returns Success when api call succeeds`() = runTest {
        val fakeMovie = Movie(
            id = 1,
            title = "Sample Movie",
            posterPath = "/poster.jpg",
            backdropPath = null,
            overview = "Sample overview",
            releaseDate = "2026-01-01",
            voteAverage = 7.5,
            genreIds = listOf(1)
        )
        val fakeResponse = MoviePagedResponse(page = 1, results = listOf(fakeMovie), totalPages = 5)
        coEvery { api.discoverMoviesByGenre(genreId = 1, page = 1) } returns fakeResponse

        val result = repository.discoverMoviesByGenre(genreId = 1, page = 1)

        assertTrue(result is Resource.Success)
        assertEquals(fakeResponse, (result as Resource.Success).data)
    }

    @Test
    fun `discoverMoviesByGenre returns Error when api call throws exception`() = runTest {
        coEvery { api.discoverMoviesByGenre(genreId = 1, page = 1) } throws IOException("Timeout")

        val result = repository.discoverMoviesByGenre(genreId = 1, page = 1)

        assertTrue(result is Resource.Error)
        assertEquals("Timeout", (result as Resource.Error).message)
    }
}
