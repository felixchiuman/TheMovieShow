package com.felix.themovieshow.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.felix.themovieshow.data.api.model.AuthorDetails
import com.felix.themovieshow.data.api.model.Review
import com.felix.themovieshow.ui.component.EmptyView
import com.felix.themovieshow.ui.component.ErrorView
import com.felix.themovieshow.ui.component.LoadingView
import com.felix.themovieshow.ui.component.ReviewCard
import com.felix.themovieshow.ui.theme.BackgroundDark
import com.felix.themovieshow.ui.theme.TheMovieShowTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ReviewListScreen(
    onBackClick: () -> Unit,
    viewModel: ReviewListViewModel = hiltViewModel()
) {
    val reviews = viewModel.reviews.collectAsLazyPagingItems()

    ReviewListContent(
        reviews = reviews,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewListContent(
    reviews: LazyPagingItems<Review>,
    onBackClick: () -> Unit
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
        val refreshState = reviews.loadState.refresh
        when {
            refreshState is LoadState.Loading && reviews.itemCount == 0 -> {
                LoadingView(modifier = Modifier.padding(padding))
            }
            refreshState is LoadState.Error && reviews.itemCount == 0 -> {
                ErrorView(
                    message = refreshState.error.message ?: "Gagal mengambil review film",
                    onRetry = reviews::retry,
                    modifier = Modifier.padding(padding)
                )
            }
            reviews.itemCount == 0 -> {
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
                    items(
                        count = reviews.itemCount,
                        key = reviews.itemKey { it.id }
                    ) { index ->
                        reviews[index]?.let { review ->
                            ReviewCard(review = review)
                        }
                    }

                    when (val appendState = reviews.loadState.append) {
                        is LoadState.Loading -> {
                            item { LoadingView() }
                        }
                        is LoadState.Error -> {
                            item {
                                ErrorView(
                                    message = appendState.error.message
                                        ?: "Gagal memuat review berikutnya",
                                    onRetry = reviews::retry
                                )
                            }
                        }
                        else -> Unit
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

/** PagingData statis untuk preview tanpa Hilt/ViewModel. */
@Composable
private fun previewPagingItems(data: List<Review>): LazyPagingItems<Review> {
    val flow = remember {
        MutableStateFlow(
            PagingData.from(
                data = data,
                sourceLoadStates = LoadStates(
                    refresh = LoadState.NotLoading(endOfPaginationReached = true),
                    prepend = LoadState.NotLoading(endOfPaginationReached = true),
                    append = LoadState.NotLoading(endOfPaginationReached = true)
                )
            )
        )
    }
    return flow.collectAsLazyPagingItems()
}

@Preview(showBackground = true, name = "Review - Loaded")
@Composable
private fun ReviewListContentPreview() {
    TheMovieShowTheme {
        ReviewListContent(
            reviews = previewPagingItems(sampleReviewList),
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Review - Empty")
@Composable
private fun ReviewListContentEmptyPreview() {
    TheMovieShowTheme {
        ReviewListContent(
            reviews = previewPagingItems(emptyList()),
            onBackClick = {}
        )
    }
}
