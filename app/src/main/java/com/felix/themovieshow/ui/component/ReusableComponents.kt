package com.felix.themovieshow.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.felix.themovieshow.data.api.model.Movie
import com.felix.themovieshow.ui.theme.AccentRed
import com.felix.themovieshow.ui.theme.TextSecondary

@Composable
fun TopHeaderGreeting(
    userName: String,
    modifier: Modifier = Modifier,
    onFavoriteClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Hello", color = TextSecondary, fontSize = 14.sp)
            Text(userName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        if (onFavoriteClick != null) {
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Favorite list",
                    tint = AccentRed
                )
            }
        }
    }
}

/**
 * Card poster movie
 */
@Composable
fun MoviePosterCard(
    movie: Movie,
    onClick: (Movie) -> Unit,
    modifier: Modifier = Modifier,
    width: Int = 110,
    height: Int = 150
) {
    Column(
        modifier = modifier
            .width(width.dp)
            .clickable { onClick(movie) }
    ) {
        AsyncImage(
            model = movie.posterUrl,
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(width.dp)
                .height(height.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = movie.title,
            color = Color.White,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Section horizontal
 */
@Composable
fun MovieRowSection(
    title: String,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    onViewAllClick: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 20.dp)) {
        SectionHeader(title = title, onViewAllClick = onViewAllClick)
        Spacer(Modifier.height(10.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            itemsIndexed(movies, key = { _, movie -> movie.id }) { index, movie ->
                MoviePosterCard(movie = movie, onClick = onMovieClick)
                // trigger load more saat 3 item terakhir kelihatan -> endless scrolling
                if (index >= movies.size - 3) {
                    LaunchedEffect(movie.id) { onLoadMore() }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Text(
            "View All",
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.clickable { onViewAllClick() }
        )
    }
}

/** Negative case: loading state -- reusable di semua screen. */
@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = AccentRed)
    }
}

/** Negative case: error state dengan tombol retry -- reusable di semua screen. */
@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, color = TextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = AccentRed)) {
            Text("Coba Lagi")
        }
    }
}

/** Negative case: empty state kalau list kosong (misal genre tidak punya movie). */
@Composable
fun EmptyView(message: String = "Tidak ada data", modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = TextSecondary, fontSize = 14.sp)
    }
}

private val previewMovie = Movie(
    id = 1,
    title = "Mortal Kombat II",
    posterPath = "/sample.jpg",
    backdropPath = "/sample_backdrop.jpg",
    overview = "Sample overview text for preview purposes.",
    releaseDate = "2026-05-07",
    voteAverage = 8.0,
    genreIds = listOf(28)
)

private val previewMovies = listOf(
    previewMovie.copy(id = 1, title = "Mortal Kombat II"),
    previewMovie.copy(id = 2, title = "Street Fighter"),
    previewMovie.copy(id = 3, title = "Tekken"),
    previewMovie.copy(id = 4, title = "Avengers Assemble")
)

// ============ PREVIEWS ============

@Preview(showBackground = true, name = "Top Header")
@Composable
private fun TopHeaderGreetingPreview() {
    TopHeaderGreeting(userName = "Felix")
}

@Preview(showBackground = true, name = "Movie Poster Card")
@Composable
private fun MoviePosterCardPreview() {
    MoviePosterCard(movie = previewMovie, onClick = {})
}

@Preview(showBackground = true, name = "Movie Row Section", widthDp = 400)
@Composable
private fun MovieRowSectionPreview() {
    MovieRowSection(
        title = "Continue Watching",
        movies = previewMovies,
        onMovieClick = {}
    )
}

@Preview(showBackground = true, name = "Section Header")
@Composable
private fun SectionHeaderPreview() {
    SectionHeader(title = "Top Trending", onViewAllClick = {})
}

@Preview(showBackground = true, name = "Loading View")
@Composable
private fun LoadingViewPreview() {
    LoadingView()
}

@Preview(showBackground = true, name = "Error View")
@Composable
private fun ErrorViewPreview() {
    ErrorView(message = "Gagal mengambil data", onRetry = {})
}

@Preview(showBackground = true, name = "Empty View")
@Composable
private fun EmptyViewPreview() {
    EmptyView("Tidak ada film untuk genre ini")
}