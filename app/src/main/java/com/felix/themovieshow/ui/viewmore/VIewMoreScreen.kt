package com.felix.themovieshow.ui.viewmore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.ui.component.EmptyView
import com.felix.themovieshow.ui.component.ErrorView
import com.felix.themovieshow.ui.component.LoadingView
import com.felix.themovieshow.ui.component.MoviePosterCard
import com.felix.themovieshow.ui.theme.BackgroundDark
import com.felix.themovieshow.ui.theme.TheMovieShowTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ViewMoreScreen(
    genreName: String,
    onBackClick: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    viewModel: ViewMoreViewModel = hiltViewModel()
) {
    // Endless scroll otomatis: akses item via movies[index] + prefetchDistance
    // di PagingConfig menggantikan LaunchedEffect { onLoadMore() } manual.
    val movies = viewModel.movies.collectAsLazyPagingItems()

    ViewMoreContent(
        genreName = genreName,
        movies = movies,
        onBackClick = onBackClick,
        onMovieClick = onMovieClick
    )
}

/** Dipisah dari ViewMoreScreen supaya bisa di-@Preview tanpa Hilt/ViewModel. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewMoreContent(
    genreName: String,
    movies: LazyPagingItems<Movie>,
    onBackClick: () -> Unit,
    onMovieClick: (Movie) -> Unit
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
        val refreshState = movies.loadState.refresh
        when {
            refreshState is LoadState.Loading && movies.itemCount == 0 -> {
                LoadingView(modifier = Modifier.padding(padding))
            }
            refreshState is LoadState.Error && movies.itemCount == 0 -> {
                ErrorView(
                    message = refreshState.error.message
                        ?: "Gagal mengambil daftar film untuk genre ini",
                    onRetry = movies::retry,
                    modifier = Modifier.padding(padding)
                )
            }
            movies.itemCount == 0 -> {
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
                    items(
                        count = movies.itemCount,
                        key = movies.itemKey { it.id }
                    ) { index ->
                        movies[index]?.let { movie ->
                            MoviePosterCard(
                                movie = movie,
                                onClick = onMovieClick,
                                width = 110,
                                height = 160
                            )
                        }
                    }

                    when (val appendState = movies.loadState.append) {
                        is LoadState.Loading -> {
                            item(span = { GridItemSpan(maxLineSpan) }) { LoadingView() }
                        }
                        is LoadState.Error -> {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                ErrorView(
                                    message = appendState.error.message
                                        ?: "Gagal memuat halaman berikutnya",
                                    onRetry = movies::retry
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

@Preview(showBackground = true)
@Composable
private fun ViewMoreContentPreview() {
    TheMovieShowTheme {
        val sampleMovies = List(9) { i ->
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
        // PagingData statis untuk preview, loadState di-set NotLoading supaya grid tampil
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
        ViewMoreContent(
            genreName = "Action",
            movies = previewFlow.collectAsLazyPagingItems(),
            onBackClick = {},
            onMovieClick = {}
        )
    }
}
