package com.felix.themovieshow.ui.viewmore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class ViewMoreUiState(
    val movies: List<Movie> = emptyList(),
    val currentPage: Int = 1,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val endReached: Boolean = false
)

@HiltViewModel
class ViewMoreViewModel @Inject constructor(
    private val repository: HomeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val genreId: Int = checkNotNull(savedStateHandle["genreId"])

    private val _uiState = MutableStateFlow(ViewMoreUiState())
    val uiState: StateFlow<ViewMoreUiState> = _uiState.asStateFlow()

    init {
        loadMovies(reset = true)
    }

    fun loadMore() {
        if (_uiState.value.isLoading || _uiState.value.endReached) return
        _uiState.update { it.copy(isLoading = true) }
        loadMovies(reset = false)
    }

    fun retry() {
        loadMovies(reset = true)
    }

    private fun loadMovies(reset: Boolean) {
        viewModelScope.launch {
            val page = if (reset) 1 else _uiState.value.currentPage + 1
            if (reset) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
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
                            isLoading = false,
                            movies = newMovies,
                            currentPage = page,
                            endReached = page >= result.data.totalPages,
                            errorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }
}