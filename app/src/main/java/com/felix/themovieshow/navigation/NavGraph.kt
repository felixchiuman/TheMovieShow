package com.felix.themovieshow.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.felix.themovieshow.ui.detail.MovieDetailScreen
import com.felix.themovieshow.ui.home.HomeScreen
import com.felix.themovieshow.ui.review.ReviewListScreen
import com.felix.themovieshow.ui.viewmore.ViewMoreScreen

object Routes {
    const val HOME = "home"
    const val DETAIL = "detail/{movieId}"
    const val REVIEWS = "reviews/{movieId}"
    const val VIEW_MORE = "viewMore/{genreId}/{genreName}"

    fun detail(movieId: Int) = "detail/$movieId"
    fun reviews(movieId: Int) = "reviews/$movieId"
    fun viewMore(genreId: Int, genreName: String) = "viewMore/$genreId/${Uri.encode(genreName)}"
}

@Composable
fun TheMovieShowNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                userName = "Felix",
                onMovieClick = { movie -> navController.navigate(Routes.detail(movie.id)) },
                onSeeAllClick = { genreId, genreName ->
                    navController.navigate(Routes.viewMore(genreId, genreName))
                }
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("movieId") { type = NavType.IntType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId") ?: return@composable
            MovieDetailScreen(
                onBackClick = { navController.popBackStack() },
                onMovieClick = { movie -> navController.navigate(Routes.detail(movie.id)) },
                onSeeAllReviewsClick = { navController.navigate(Routes.reviews(movieId)) }
            )
        }

        composable(
            route = Routes.REVIEWS,
            arguments = listOf(navArgument("movieId") { type = NavType.IntType })
        ) {
            ReviewListScreen(onBackClick = { navController.popBackStack() })
        }

        composable(
            route = Routes.VIEW_MORE,
            arguments = listOf(
                navArgument("genreId") { type = NavType.IntType },
                navArgument("genreName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val genreName = backStackEntry.arguments?.getString("genreName")?.let { Uri.decode(it) } ?: "Movies"
            ViewMoreScreen(
                genreName = genreName,
                onBackClick = { navController.popBackStack() },
                onMovieClick = { movie -> navController.navigate(Routes.detail(movie.id)) }
            )
        }
    }
}