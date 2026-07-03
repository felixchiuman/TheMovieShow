package com.felix.themovieshow.repository

import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.api.model.PopularMovieResponse
import com.felix.themovieshow.data.api.model.TopRatedMovieResponse
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

    private val sampleMovie = Movie(
        id = 1,
        title = "Test Movie",
        posterPath = "/path.jpg",
        backdropPath = "/backdrop.jpg",
        overview = "Overview",
        releaseDate = "2024-01-01",
        voteAverage = 8.5,
        genreIds = listOf(1, 2)
    )

    @Before
    fun setUp() {
        api = mockk()
        repository = HomeRepositoryImpl(api)
    }

    @Test
    fun `getPopularMovie returns Success when api call succeeds`() = runTest {
        val expectedResponse = PopularMovieResponse(
            page = 1,
            results = listOf(sampleMovie),
            totalPages = 10,
            totalResults = 100
        )
        coEvery { api.getMoviePopular(1) } returns expectedResponse

        val result = repository.getPopularMovie(1)

        assertTrue(result is Resource.Success)
        assertEquals(expectedResponse, (result as Resource.Success).data)
    }

    @Test
    fun `getPopularMovie returns Error when api call throws exception`() = runTest {
        val errorMessage = "Network Error"
        coEvery { api.getMoviePopular(1) } throws IOException(errorMessage)

        val result = repository.getPopularMovie(1)

        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, (result as Resource.Error).message)
    }

    @Test
    fun `getTopRated returns Success when api call succeeds`() = runTest {
        val expectedResponse = TopRatedMovieResponse(
            page = 1,
            results = listOf(sampleMovie),
            totalPages = 5,
            totalResults = 50
        )
        coEvery { api.getTopRated(1) } returns expectedResponse

        val result = repository.getTopRated(1)

        assertTrue(result is Resource.Success)
        assertEquals(expectedResponse, (result as Resource.Success).data)
    }

    @Test
    fun `getTopRated returns Error when api call throws exception`() = runTest {
        val errorMessage = "Server Error"
        coEvery { api.getTopRated(1) } throws Exception(errorMessage)

        val result = repository.getTopRated(1)

        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, (result as Resource.Error).message)
    }
}
