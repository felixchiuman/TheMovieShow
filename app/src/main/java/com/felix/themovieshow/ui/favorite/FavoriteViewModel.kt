package com.felix.themovieshow.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoriteUiState(
    val isLoading: Boolean = true,
    val favorites: List<Movie> = emptyList()
)

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    val uiState: StateFlow<FavoriteUiState> =
        favoriteRepository.getFavorites()
            .map { movies -> FavoriteUiState(isLoading = false, favorites = movies) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = FavoriteUiState(isLoading = true)
            )

    fun removeFavorite(movieId: Int) {
        viewModelScope.launch {
            favoriteRepository.removeFavorite(movieId)
        }
    }
}
