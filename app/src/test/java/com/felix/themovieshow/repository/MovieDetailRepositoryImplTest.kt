package com.felix.themovieshow.repository

import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.api.model.Video
import com.felix.themovieshow.data.api.model.VideoResponse
import com.felix.themovieshow.data.api.network.ApiService
import com.felix.themovieshow.data.repository.MovieDetailRepositoryImpl
import com.felix.themovieshow.data.resource.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class MovieDetailRepositoryImplTest {

    private lateinit var api: ApiService
    private lateinit var repository: MovieDetailRepositoryImpl

    private val sampleMovie = Movie(
        id = 931285,
        title = "Mortal Kombat II",
        posterPath = "/poster.jpg",
        backdropPath = "/backdrop.jpg",
        overview = "Sample overview",
        releaseDate = "2026-05-07",
        voteAverage = 8.0,
        genreIds = listOf(28)
    )

    @Before
    fun setUp() {
        api = mockk()
        repository = MovieDetailRepositoryImpl(api)
    }

    @Test
    fun `getMovieDetail returns Success when api call succeeds`() = runTest {
        coEvery { api.getMovieDetail(931285) } returns sampleMovie

        val result = repository.getMovieDetail(931285)

        assertTrue(result is Resource.Success)
        assertEquals(sampleMovie, (result as Resource.Success).data)
    }

    @Test
    fun `getMovieDetail returns Error when api call throws exception`() = runTest {
        coEvery { api.getMovieDetail(931285) } throws IOException("Movie not found")

        val result = repository.getMovieDetail(931285)

        assertTrue(result is Resource.Error)
        assertEquals("Movie not found", (result as Resource.Error).message)
    }

    @Test
    fun `getMovieTrailer returns Success with trailer when a YouTube Trailer exists`() = runTest {
        val trailer = Video(id = "1", key = "abc123", site = "YouTube", type = "Trailer")
        val teaser = Video(id = "2", key = "xyz789", site = "YouTube", type = "Teaser")
        coEvery { api.getMovieVideos(931285) } returns VideoResponse(results = listOf(teaser, trailer))

        val result = repository.getMovieTrailer(931285)

        assertTrue(result is Resource.Success)
        assertEquals(trailer, (result as Resource.Success).data)
    }

    @Test
    fun `getMovieTrailer returns Success with null when no YouTube Trailer exists`() = runTest {
        val teaser = Video(id = "2", key = "xyz789", site = "YouTube", type = "Teaser")
        coEvery { api.getMovieVideos(931285) } returns VideoResponse(results = listOf(teaser))

        val result = repository.getMovieTrailer(931285)

        assertTrue(result is Resource.Success)
        assertNull((result as Resource.Success).data)
    }

    @Test
    fun `getMovieTrailer returns Error when api call throws exception`() = runTest {
        coEvery { api.getMovieVideos(931285) } throws IOException("Failed to fetch videos")

        val result = repository.getMovieTrailer(931285)

        assertTrue(result is Resource.Error)
        assertEquals("Failed to fetch videos", (result as Resource.Error).message)
    }
}
