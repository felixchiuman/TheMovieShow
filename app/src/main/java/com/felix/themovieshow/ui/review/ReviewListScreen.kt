package com.felix.themovieshow.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.felix.themovieshow.data.api.model.AuthorDetails
import com.felix.themovieshow.data.api.model.Review
import com.felix.themovieshow.ui.component.EmptyView
import com.felix.themovieshow.ui.component.ErrorView
import com.felix.themovieshow.ui.component.LoadingView
import com.felix.themovieshow.ui.component.ReviewCard
import com.felix.themovieshow.ui.theme.BackgroundDark
import com.felix.themovieshow.ui.theme.TheMovieShowTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewListScreen(
    onBackClick: () -> Unit,
    viewModel: ReviewListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ReviewListContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetry = viewModel::retry,
        onLoadMore = viewModel::loadMore
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewListContent(
    uiState: ReviewListUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Reviews", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        }
    ) { padding ->
        when {
            uiState.reviews.isEmpty() && uiState.isLoading -> {
                LoadingView(modifier = Modifier.padding(padding))
            }
            uiState.errorMessage != null && uiState.reviews.isEmpty() -> {
                ErrorView(
                    message = uiState.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.padding(padding)
                )
            }
            uiState.reviews.isEmpty() -> {
                EmptyView("Belum ada review untuk film ini", modifier = Modifier.padding(padding))
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundDark)
                        .padding(padding),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(uiState.reviews, key = { _, review -> review.id }) { index, review ->
                        ReviewCard(review = review)
                        if (index >= uiState.reviews.size - 3) {
                            LaunchedEffect(review.id) { onLoadMore() }
                        }
                    }
                    if (uiState.isLoading) {
                        item { LoadingView() }
                    }
                }
            }
        }
    }
}

private val sampleReviewList = listOf(
    Review(
        id = "1",
        author = "Felix C.",
        content = "Aksinya keren banget, efek visual-nya juga niat. Wajib nonton di IMAX!",
        authorDetails = AuthorDetails(rating = 9.0)
    ),
    Review(
        id = "2",
        author = "Jane D.",
        content = "Ceritanya standar tapi fight scene-nya menghibur.",
        authorDetails = AuthorDetails(rating = 7.5)
    ),
    Review(
        id = "3",
        author = "Budi S.",
        content = "Salah satu film fighting terbaik tahun ini, koreografinya juara.",
        authorDetails = AuthorDetails(rating = 8.5)
    )
)

@Preview(showBackground = true, name = "Review - Loaded")
@Composable
private fun ReviewListContentPreview() {
    TheMovieShowTheme {
        ReviewListContent(
            uiState = ReviewListUiState(reviews = sampleReviewList),
            onBackClick = {},
            onRetry = {},
            onLoadMore = {}
        )
    }
}

@Preview(showBackground = true, name = "Review - Loading")
@Composable
private fun ReviewListContentLoadingPreview() {
    TheMovieShowTheme {
        ReviewListContent(
            uiState = ReviewListUiState(isLoading = true),
            onBackClick = {},
            onRetry = {},
            onLoadMore = {}
        )
    }
}

@Preview(showBackground = true, name = "Review - Empty")
@Composable
private fun ReviewListContentEmptyPreview() {
    TheMovieShowTheme {
        ReviewListContent(
            uiState = ReviewListUiState(reviews = emptyList()),
            onBackClick = {},
            onRetry = {},
            onLoadMore = {}
        )
    }
}

@Preview(showBackground = true, name = "Review - Error")
@Composable
private fun ReviewListContentErrorPreview() {
    TheMovieShowTheme {
        ReviewListContent(
            uiState = ReviewListUiState(errorMessage = "Gagal mengambil review"),
            onBackClick = {},
            onRetry = {},
            onLoadMore = {}
        )
    }
}