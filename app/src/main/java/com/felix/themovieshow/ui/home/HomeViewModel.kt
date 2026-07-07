package com.felix.themovieshow.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.felix.themovieshow.data.api.model.Genre
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.repository.HomeRepository
import com.felix.themovieshow.data.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State movies (list, currentPage, isLoadingMovies) pindah ke Paging 3.
 * UiState sekarang cuma pegang state genre.
 */
data class HomeUiState(
    val isLoadingGenres: Boolean = false,
    val genres: List<Genre> = emptyList(),
    val selectedGenreId: Int? = null,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * Ganti genre -> flatMapLatest cancel Pager lama dan bikin yang baru.
     * Ini pengganti manual reset (movies = emptyList(), currentPage = 1)
     * di onGenreSelected yang lama, sekaligus menutup celah race condition
     * kalau user ganti genre saat page berikutnya masih loading.
     *
     * Filter seenIds = pengganti distinctBy { it.id } (jaring pengaman
     * duplicate movie antar page dari TMDB).
     */
    val movies: Flow<PagingData<Movie>> = _uiState
        .map { it.selectedGenreId }
        .distinctUntilChanged()
        .filterNotNull()
        .flatMapLatest { genreId ->
            repository.getMoviesByGenrePaged(genreId).map { pagingData ->
                val seenIds = mutableSetOf<Int>()
                pagingData.filter { movie -> seenIds.add(movie.id) }
            }
        }
        .cachedIn(viewModelScope)

    init {
        loadGenres()
    }

    fun loadGenres() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingGenres = true, errorMessage = null) }
            when (val result = repository.getGenres()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingGenres = false,
                            genres = result.data,
                            // selectedGenreId berubah -> movies flow otomatis mulai
                            selectedGenreId = result.data.firstOrNull()?.id
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoadingGenres = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun onGenreSelected(genre: Genre) {
        // distinctUntilChanged di movies flow sudah jaga dari re-fetch genre
        // yang sama; guard ini cuma menghindari update state yang tidak perlu.
        if (genre.id == _uiState.value.selectedGenreId) return
        _uiState.update { it.copy(selectedGenreId = genre.id) }
    }
}
