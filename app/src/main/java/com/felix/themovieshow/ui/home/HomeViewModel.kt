package com.felix.themovieshow.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felix.themovieshow.data.api.model.Genre
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.repository.HomeRepository
import com.felix.themovieshow.data.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoadingGenres: Boolean = false,
    val genres: List<Genre> = emptyList(),
    val selectedGenreId: Int? = null,
    val movies: List<Movie> = emptyList(),
    val currentPage: Int = 1,
    val isLoadingMovies: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadGenres()
    }

    fun loadGenres() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingGenres = true, errorMessage = null) }
            when (val result = repository.getGenres()) {
                is Resource.Success -> {
                    val firstGenre = result.data.firstOrNull()
                    _uiState.update {
                        it.copy(
                            isLoadingGenres = false,
                            genres = result.data,
                            selectedGenreId = firstGenre?.id
                        )
                    }
                    firstGenre?.let { loadMoviesByGenre(it.id, reset = true) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoadingGenres = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun onGenreSelected(genre: Genre) {
        if (genre.id == _uiState.value.selectedGenreId) return
        _uiState.update { it.copy(selectedGenreId = genre.id, movies = emptyList(), currentPage = 1) }
        loadMoviesByGenre(genre.id, reset = true)
    }

    fun loadMoreMovies() {
        val genreId = _uiState.value.selectedGenreId ?: return
        if (_uiState.value.isLoadingMovies) return
        _uiState.update { it.copy(isLoadingMovies = true) }
        loadMoviesByGenre(genreId, reset = false)
    }

    private fun loadMoviesByGenre(genreId: Int, reset: Boolean) {
        viewModelScope.launch {
            val page = if (reset) 1 else _uiState.value.currentPage + 1
            if (reset) {
                _uiState.update { it.copy(isLoadingMovies = true) }
            }

            when (val result = repository.discoverMoviesByGenre(genreId, page)) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        val newMovies = if (reset) {
                            result.data.results
                        } else {
                            (state.movies + result.data.results).distinctBy { it.id }
                        }
                        state.copy(
                            isLoadingMovies = false,
                            movies = newMovies,
                            currentPage = page,
                            errorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoadingMovies = false, errorMessage = result.message) }
                }
            }
        }
    }
}