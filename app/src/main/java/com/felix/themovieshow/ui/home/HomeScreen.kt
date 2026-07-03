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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.ui.component.EmptyView
import com.felix.themovieshow.ui.component.ErrorView
import com.felix.themovieshow.ui.component.GenreChipRow
import com.felix.themovieshow.ui.component.LoadingView
import com.felix.themovieshow.ui.component.MovieRowSection
import com.felix.themovieshow.ui.component.TopHeaderGreeting
import androidx.compose.ui.tooling.preview.Preview
import com.felix.themovieshow.data.api.model.Genre
import com.felix.themovieshow.ui.theme.TheMovieShowTheme
import com.felix.themovieshow.ui.theme.BackgroundDark

@Composable
fun HomeScreen(
    userName: String,
    onMovieClick: (Movie) -> Unit,
    onSeeAllClick: (Int, String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        userName = userName,
        uiState = uiState,
        onMovieClick = onMovieClick,
        onSeeAllClick = onSeeAllClick,
        onGenreSelected = viewModel::onGenreSelected,
        onLoadMoreMovies = viewModel::loadMoreMovies,
        onRetryLoadGenres = viewModel::loadGenres
    )
}

@Composable
fun HomeScreenContent(
    userName: String,
    uiState: HomeUiState,
    onMovieClick: (Movie) -> Unit,
    onSeeAllClick: (Int, String) -> Unit,
    onGenreSelected: (Genre) -> Unit,
    onLoadMoreMovies: () -> Unit,
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

                    when {
                        uiState.movies.isEmpty() && uiState.isLoadingMovies -> LoadingView()
                        uiState.movies.isEmpty() -> EmptyView("Tidak ada film untuk genre ini")
                        else -> {
                            MovieRowSection(
                                title = "Continue Watching",
                                movies = uiState.movies,
                                onMovieClick = onMovieClick,
                                onLoadMore = onLoadMoreMovies,
                                onViewAllClick = {
                                    uiState.selectedGenreId?.let { onSeeAllClick(it, selectedGenreName) }
                                }
                            )
                            MovieRowSection(
                                title = "Top Trending",
                                movies = uiState.movies.take(10),
                                onMovieClick = onMovieClick,
                                onViewAllClick = {
                                    uiState.selectedGenreId?.let { onSeeAllClick(it, selectedGenreName) }
                                }
                            )
                        }
                    }

                    if (uiState.isLoadingMovies && uiState.movies.isNotEmpty()) {
                        LoadingView()
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
        HomeScreenContent(
            userName = "Felix",
            uiState = HomeUiState(
                genres = listOf(
                    Genre(1, "Action"),
                    Genre(2, "Adventure"),
                    Genre(3, "Comedy"),
                    Genre(4, "Drama")
                ),
                selectedGenreId = 1,
                movies = listOf(
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
            ),
            onMovieClick = {},
            onSeeAllClick = { _, _ -> },
            onGenreSelected = {},
            onLoadMoreMovies = {},
            onRetryLoadGenres = {}
        )
    }
}