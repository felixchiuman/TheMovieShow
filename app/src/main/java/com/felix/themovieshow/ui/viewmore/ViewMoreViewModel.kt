package com.felix.themovieshow.ui.viewmore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class ViewMoreViewModel @Inject constructor(
    repository: HomeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val genreId: Int = checkNotNull(savedStateHandle["genreId"])

    /**
     * State loading/error/endReached yang dulu di-manage manual di UiState
     * sekarang datang dari LoadState milik Paging (movies.loadState di UI).
     *
     * Filter seenIds = pengganti distinctBy { it.id } yang lama. TMDB kadang
     * return movie yang sama di dua page berbeda, dan duplicate key bikin
     * LazyVerticalGrid crash. Set-nya dibuat ulang tiap generation baru
     * (refresh), jadi tidak bocor antar refresh.
     *
     * cachedIn(viewModelScope) wajib: tanpa ini PagingData di-restart tiap
     * recomposition/rotation.
     */
    val movies: Flow<PagingData<Movie>> = repository
        .getMoviesByGenrePaged(genreId)
        .map { pagingData ->
            val seenIds = mutableSetOf<Int>()
            pagingData.filter { movie -> seenIds.add(movie.id) }
        }
        .cachedIn(viewModelScope)
}
