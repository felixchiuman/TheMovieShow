package com.felix.themovieshow.ui.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felix.themovieshow.data.api.model.Review
import com.felix.themovieshow.data.repository.ReviewRepository
import com.felix.themovieshow.data.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewListUiState(
    val reviews: List<Review> = emptyList(),
    val currentPage: Int = 1,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val endReached: Boolean = false
)

@HiltViewModel
class ReviewListViewModel @Inject constructor(
    private val repository: ReviewRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val movieId: Int = checkNotNull(savedStateHandle["movieId"])

    private val _uiState = MutableStateFlow(ReviewListUiState())
    val uiState: StateFlow<ReviewListUiState> = _uiState.asStateFlow()

    init {
        loadReviews(reset = true)
    }

    fun loadMore() {
        if (_uiState.value.isLoading || _uiState.value.endReached) return
        _uiState.update { it.copy(isLoading = true) }
        loadReviews(reset = false)
    }

    fun retry() {
        loadReviews(reset = true)
    }

    private fun loadReviews(reset: Boolean) {
        viewModelScope.launch {
            val page = if (reset) 1 else _uiState.value.currentPage + 1
            if (reset) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            when (val result = repository.getMovieReviews(movieId, page)) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        val newReviews = if (reset) {
                            result.data.results
                        } else {
                            // distinctBy sebagai jaring pengaman kedua, sama seperti di
                            // HomeViewModel -- kalaupun API return data overlap antar page.
                            (state.reviews + result.data.results).distinctBy { it.id }
                        }
                        state.copy(
                            isLoading = false,
                            reviews = newReviews,
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