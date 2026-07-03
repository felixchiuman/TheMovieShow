package com.felix.themovieshow.ui.detail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.felix.themovieshow.data.api.model.AuthorDetails
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.data.api.model.Review
import com.felix.themovieshow.ui.component.ErrorView
import com.felix.themovieshow.ui.component.LoadingView
import com.felix.themovieshow.ui.component.ReviewPreviewSection
import com.felix.themovieshow.ui.component.YoutubePlayer
import com.felix.themovieshow.ui.theme.AccentRed
import com.felix.themovieshow.ui.theme.BackgroundDark
import com.felix.themovieshow.ui.theme.TextSecondary
import com.felix.themovieshow.ui.theme.TheMovieShowTheme

@Composable
fun MovieDetailScreen(
    onBackClick: () -> Unit,
    onSeeAllReviewsClick: () -> Unit,
    viewModel: MovieDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    MovieDetailContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onSeeAllReviewsClick = onSeeAllReviewsClick,
        onToggleFavorite = viewModel::toggleSave,
        onShareClick = {
            uiState.movie?.let { movie ->
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Check out this movie: ${movie.title}\nhttps://www.themoviedb.org/movie/${movie.id}"
                    )
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            }
        },
        onRetry = viewModel::loadMovieDetail
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailContent(
    uiState: MovieDetailUiState,
    onBackClick: () -> Unit,
    onSeeAllReviewsClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShareClick: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            if (uiState.movie != null) {
                Surface(
                    color = BackgroundDark,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onShareClick,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }

                        Button(
                            onClick = onToggleFavorite,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isSaved) Color.White.copy(alpha = 0.1f) else AccentRed
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState.isSaved) Icons.Filled.Favorite
                                else Icons.Filled.FavoriteBorder,
                                contentDescription = null,
                                tint = if (uiState.isSaved) AccentRed else Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (uiState.isSaved) "Remove from Favorite" else "Add to Favorite",
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        when {
            uiState.isLoading && uiState.movie == null -> {
                LoadingView(modifier = Modifier.padding(padding))
            }
            uiState.errorMessage != null && uiState.movie == null -> {
                ErrorView(
                    message = uiState.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.padding(padding)
                )
            }
            uiState.movie != null -> {
                val movie = uiState.movie
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundDark)
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    Box {
                        val trailerKey = uiState.trailerKey
                        if (trailerKey != null) {
                            YoutubePlayer(videoKey = trailerKey)
                        } else {
                            AsyncImage(
                                model = movie.backdropUrl ?: movie.posterUrl,
                                contentDescription = movie.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                            )
                        }
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }

                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(movie.title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Row {
                            movie.releaseDate?.take(4)?.let { year ->
                                Text("$year  •  ", color = TextSecondary, fontSize = 13.sp)
                            }
                            Text("★ ${"%.1f".format(movie.voteAverage)}", color = TextSecondary, fontSize = 13.sp)
                        }

                        Spacer(Modifier.height(16.dp))
                        Text(movie.overview, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
                    }

                    if (uiState.reviewPreview.isNotEmpty()) {
                        ReviewPreviewSection(
                            reviews = uiState.reviewPreview,
                            onSeeAllClick = onSeeAllReviewsClick
                        )
                    }
                }
            }
        }
    }
}

private val sampleMovie = Movie(
    id = 931285,
    title = "Mortal Kombat II",
    posterPath = "/sample_poster.jpg",
    backdropPath = null,
    overview = "The fan favorite champions—now joined by Johnny Cage himself—are pitted " +
            "against one another in the ultimate, no-holds barred, gory battle to defeat the " +
            "dark rule of Shao Kahn.",
    releaseDate = "2026-05-07",
    voteAverage = 8.0,
    genreIds = listOf(28, 12)
)

private val sampleReviews = listOf(
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
    )
)

// ============ PREVIEWS ============

@Preview(showBackground = true, name = "Detail - Loaded")
@Composable
private fun MovieDetailContentPreview() {
    TheMovieShowTheme {
        MovieDetailContent(
            uiState = MovieDetailUiState(
                isLoading = false,
                movie = sampleMovie,
                trailerKey = null, // null supaya preview tampilin backdrop image, bukan YoutubePlayer
                reviewPreview = sampleReviews
            ),
            onBackClick = {},
            onSeeAllReviewsClick = {},
            onToggleFavorite = {},
            onShareClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Detail - Loading")
@Composable
private fun MovieDetailContentLoadingPreview() {
    TheMovieShowTheme {
        MovieDetailContent(
            uiState = MovieDetailUiState(isLoading = true),
            onBackClick = {},
            onSeeAllReviewsClick = {},
            onToggleFavorite = {},
            onShareClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Detail - Error")
@Composable
private fun MovieDetailContentErrorPreview() {
    TheMovieShowTheme {
        MovieDetailContent(
            uiState = MovieDetailUiState(
                isLoading = false,
                errorMessage = "Gagal mengambil detail film"
            ),
            onBackClick = {},
            onSeeAllReviewsClick = {},
            onToggleFavorite = {},
            onShareClick = {},
            onRetry = {}
        )
    }
}