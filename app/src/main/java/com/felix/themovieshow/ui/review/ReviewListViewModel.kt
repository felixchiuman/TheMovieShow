package com.felix.themovieshow.ui.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.felix.themovieshow.data.api.model.Review
import com.felix.themovieshow.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class ReviewListViewModel @Inject constructor(
    repository: ReviewRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val movieId: Int = checkNotNull(savedStateHandle["movieId"])

    /**
     * Loading/error/endReached sekarang di-handle LoadState Paging 3.
     * Filter seenIds = pengganti distinctBy { it.id } yang lama, jaring
     * pengaman kalau API return review overlap antar page.
     */
    val reviews: Flow<PagingData<Review>> = repository
        .getMovieReviewsPaged(movieId)
        .map { pagingData ->
            val seenIds = mutableSetOf<String>()
            pagingData.filter { review -> seenIds.add(review.id) }
        }
        .cachedIn(viewModelScope)
}
