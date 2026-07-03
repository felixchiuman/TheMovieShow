package com.felix.themovieshow.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.api.model.Review
import com.felix.themovieshow.data.repository.HomeRepository
import com.felix.themovieshow.data.repository.MovieDetailRepository
import com.felix.themovieshow.data.repository.ReviewRepository
import com.felix.themovieshow.data.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MovieDetailUiState(
    val isLoading: Boolean = false,
    val movie: Movie? = null,
    val trailerKey: String? = null,
    val reviewPreview: List<Review> = emptyList(),
    val relatedMovies: List<Movie> = emptyList(),
    val errorMessage: String? = null,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false
)

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val detailRepository: MovieDetailRepository,
    private val reviewRepository: ReviewRepository,
    private val homeRepository: HomeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val movieId: Int = checkNotNull(savedStateHandle["movieId"])

    private val _uiState = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    init {
        loadMovieDetail()
    }

    fun loadMovieDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = detailRepository.getMovieDetail(movieId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, movie = result.data) }
                    loadTrailer()
                    loadReviewPreview()
                    result.data.genreIds.firstOrNull()?.let { loadRelatedMovies(it) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    private fun loadTrailer() {
        viewModelScope.launch {
            when (val result = detailRepository.getMovieTrailer(movieId)) {
                is Resource.Success -> _uiState.update { it.copy(trailerKey = result.data?.key) }
                is Resource.Error -> { /* trailer opsional, tidak block UI utama */ }
            }
        }
    }

    private fun loadReviewPreview() {
        viewModelScope.launch {
            when (val result = reviewRepository.getMovieReviews(movieId, page = 1)) {
                is Resource.Success -> _uiState.update { it.copy(reviewPreview = result.data.results.take(3)) }
                is Resource.Error -> { /* review preview opsional */ }
            }
        }
    }

    private fun loadRelatedMovies(genreId: Int) {
        viewModelScope.launch {
            when (val result = homeRepository.discoverMoviesByGenre(genreId, page = 1)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(relatedMovies = result.data.results.filter { m -> m.id != movieId })
                    }
                }
                is Resource.Error -> Unit
            }
        }
    }

    fun toggleLike() {
        _uiState.update { it.copy(isLiked = !it.isLiked) }
    }

    fun toggleSave() {
        _uiState.update { it.copy(isSaved = !it.isSaved) }
    }
}
