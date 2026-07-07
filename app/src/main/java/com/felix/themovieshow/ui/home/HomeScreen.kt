package com.felix.themovieshow.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.felix.themovieshow.data.api.model.Genre
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.ui.component.EmptyView
import com.felix.themovieshow.ui.component.ErrorView
import com.felix.themovieshow.ui.component.GenreChipRow
import com.felix.themovieshow.ui.component.LoadingView
import com.felix.themovieshow.ui.component.MovieRowSectionPaged
import com.felix.themovieshow.ui.component.TopHeaderGreeting
import com.felix.themovieshow.ui.theme.BackgroundDark
import com.felix.themovieshow.ui.theme.TheMovieShowTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun HomeScreen(
    userName: String,
    onMovieClick: (Movie) -> Unit,
    onSeeAllClick: (Int, String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val movies = viewModel.movies.collectAsLazyPagingItems()

    HomeScreenContent(
        userName = userName,
        uiState = uiState,
        movies = movies,
        onMovieClick = onMovieClick,
        onSeeAllClick = onSeeAllClick,
        onGenreSelected = viewModel::onGenreSelected,
        onRetryLoadGenres = viewModel::loadGenres
    )
}

@Composable
fun HomeScreenContent(
    userName: String,
    uiState: HomeUiState,
    movies: LazyPagingItems<Movie>,
    onMovieClick: (Movie) -> Unit,
    onSeeAllClick: (Int, String) -> Unit,
    onGenreSelected: (Genre) -> Unit,
    onRetryLoadGenres: () -> Unit
) {
    val selectedGenreName = uiState.genres.firstOrNull { it.id == uiState.selectedGenreId }?.name ?: "Movies"

    Scaffold(containerColor = BackgroundDark) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            TopHeaderGreeting(userName = userName)

            when {
                uiState.isLoadingGenres -> LoadingView()
                uiState.errorMessage != null && uiState.genres.isEmpty() -> {
                    ErrorView(message = uiState.errorMessage, onRetry = onRetryLoadGenres)
                }
                else -> {
                    GenreChipRow(
                        genres = uiState.genres,
                        selectedGenreId = uiState.selectedGenreId,
                        onGenreSelected = onGenreSelected
                    )

                    Spacer(Modifier.height(8.dp))

                    val movieRefresh = movies.loadState.refresh
                    when {
                        uiState.selectedGenreId == null -> {
                            EmptyView("Tidak ada film untuk genre ini")
                        }
                        movieRefresh is LoadState.Loading && movies.itemCount == 0 -> {
                            LoadingView()
                        }
                        movieRefresh is LoadState.Error && movies.itemCount == 0 -> {
                            ErrorView(
                                message = movieRefresh.error.message
                                    ?: "Gagal mengambil daftar film untuk genre ini",
                                onRetry = movies::retry
                            )
                        }
                        movies.itemCount == 0 -> {
                            EmptyView("Tidak ada film untuk genre ini")
                        }
                        else -> {
                            MovieRowSectionPaged(
                                title = "Continue Watching",
                                movies = movies,
                                onMovieClick = onMovieClick,
                                onViewAllClick = {
                                    uiState.selectedGenreId?.let { onSeeAllClick(it, selectedGenreName) }
                                }
                            )
                            MovieRowSectionPaged(
                                title = "Top Trending",
                                movies = movies,
                                maxItems = 10, // pengganti movies.take(10) yang lama
                                onMovieClick = onMovieClick,
                                onViewAllClick = {
                                    uiState.selectedGenreId?.let { onSeeAllClick(it, selectedGenreName) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    TheMovieShowTheme {
        val sampleMovies = listOf(
            Movie(
                id = 1,
                title = "Inception",
                posterPath = "/sample_poster.jpg",
                backdropPath = null,
                overview = "A thief who steals corporate secrets...",
                releaseDate = "2010-07-15",
                voteAverage = 8.8,
                genreIds = listOf(1, 2)
            ),
            Movie(
                id = 2,
                title = "The Dark Knight",
                posterPath = "/sample_poster.jpg",
                backdropPath = null,
                overview = "When the menace known as the Joker...",
                releaseDate = "2008-07-18",
                voteAverage = 9.0,
                genreIds = listOf(1, 4)
            )
        )
        val previewFlow = remember {
            MutableStateFlow(
                PagingData.from(
                    data = sampleMovies,
                    sourceLoadStates = LoadStates(
                        refresh = LoadState.NotLoading(endOfPaginationReached = true),
                        prepend = LoadState.NotLoading(endOfPaginationReached = true),
                        append = LoadState.NotLoading(endOfPaginationReached = true)
                    )
                )
            )
        }
        HomeScreenContent(
            userName = "Felix",
            uiState = HomeUiState(
                genres = listOf(
                    Genre(1, "Action"),
                    Genre(2, "Adventure"),
                    Genre(3, "Comedy"),
                    Genre(4, "Drama")
                ),
                selectedGenreId = 1
            ),
            movies = previewFlow.collectAsLazyPagingItems(),
            onMovieClick = {},
            onSeeAllClick = { _, _ -> },
            onGenreSelected = {},
            onRetryLoadGenres = {}
        )
    }
}
