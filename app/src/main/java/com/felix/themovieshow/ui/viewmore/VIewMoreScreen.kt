package com.felix.themovieshow.ui.viewmore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.ui.component.EmptyView
import com.felix.themovieshow.ui.component.ErrorView
import com.felix.themovieshow.ui.component.LoadingView
import com.felix.themovieshow.ui.component.MoviePosterCard
import com.felix.themovieshow.ui.theme.BackgroundDark
import com.felix.themovieshow.ui.theme.TheMovieShowTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewMoreScreen(
    genreName: String,
    onBackClick: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    viewModel: ViewMoreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ViewMoreContent(
        genreName = genreName,
        uiState = uiState,
        onBackClick = onBackClick,
        onMovieClick = onMovieClick,
        onLoadMore = viewModel::loadMore,
        onRetry = viewModel::retry
    )
}

/** Dipisah dari ViewMoreScreen supaya bisa di-@Preview tanpa Hilt/ViewModel. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewMoreContent(
    genreName: String,
    uiState: ViewMoreUiState,
    onBackClick: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text(genreName, color = Color.White) },
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
            uiState.movies.isEmpty() && uiState.isLoading -> {
                LoadingView(modifier = Modifier.padding(padding))
            }
            uiState.errorMessage != null && uiState.movies.isEmpty() -> {
                ErrorView(
                    message = uiState.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.padding(padding)
                )
            }
            uiState.movies.isEmpty() -> {
                EmptyView("Tidak ada film untuk genre ini", modifier = Modifier.padding(padding))
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundDark)
                        .padding(padding),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(uiState.movies, key = { _, movie -> movie.id }) { index, movie ->
                        MoviePosterCard(
                            movie = movie,
                            onClick = onMovieClick,
                            width = 110,
                            height = 160
                        )
                        if (index >= uiState.movies.size - 6) {
                            LaunchedEffect(movie.id) { onLoadMore() }
                        }
                    }
                    if (uiState.isLoading) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            LoadingView()
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ViewMoreContentPreview() {
    TheMovieShowTheme {
        ViewMoreContent(
            genreName = "Action",
            uiState = ViewMoreUiState(
                movies = List(9) { i ->
                    Movie(
                        id = i,
                        title = "Movie $i",
                        posterPath = null,
                        backdropPath = null,
                        overview = "",
                        releaseDate = "2026-01-01",
                        voteAverage = 7.5,
                        genreIds = listOf(1)
                    )
                }
            ),
            onBackClick = {},
            onMovieClick = {},
            onLoadMore = {},
            onRetry = {}
        )
    }
}