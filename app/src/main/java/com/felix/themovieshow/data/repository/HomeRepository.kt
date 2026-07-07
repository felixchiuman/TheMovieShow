package com.felix.themovieshow.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.felix.themovieshow.data.api.model.Genre
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.api.model.MoviePagedResponse
import com.felix.themovieshow.data.api.network.ApiService
import com.felix.themovieshow.data.paging.MoviePagingSource
import com.felix.themovieshow.data.resource.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface HomeRepository {
    suspend fun getGenres(): Resource<List<Genre>>

    /** Masih dipakai HomeScreen (dua row horizontal dari data yang sama). */
    suspend fun discoverMoviesByGenre(genreId: Int, page: Int): Resource<MoviePagedResponse>

    /** Paging 3 untuk endless scrolling grid di ViewMoreScreen. */
    fun getMoviesByGenrePaged(genreId: Int): Flow<PagingData<Movie>>
}

@Singleton
class HomeRepositoryImpl @Inject constructor(
    private val api: ApiService
) : HomeRepository {

    override suspend fun getGenres(): Resource<List<Genre>> {
        return try {
            val response = api.getGenres()
            Resource.Success(response.genres)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal mengambil daftar genre")
        }
    }

    override suspend fun discoverMoviesByGenre(genreId: Int, page: Int): Resource<MoviePagedResponse> {
        return try {
            val response = api.discoverMoviesByGenre(genreId, page)
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal mengambil daftar film untuk genre ini")
        }
    }

    override fun getMoviesByGenrePaged(genreId: Int): Flow<PagingData<Movie>> =
        Pager(
            config = PagingConfig(
                pageSize = MoviePagingSource.PAGE_SIZE,
                initialLoadSize = MoviePagingSource.PAGE_SIZE, // TMDB fixed 20/page
                prefetchDistance = 6, // sama dengan trigger manual sebelumnya (index >= size - 6)
                enablePlaceholders = false
            ),
            pagingSourceFactory = { MoviePagingSource(api, genreId) }
        ).flow
}
