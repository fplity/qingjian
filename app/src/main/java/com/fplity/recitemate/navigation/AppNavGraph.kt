package com.fplity.recitemate.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fplity.recitemate.data.local.FontSize
import com.fplity.recitemate.data.local.ReadingTheme
import com.fplity.recitemate.data.model.Article
import com.fplity.recitemate.ui.screens.DetailScreen
import com.fplity.recitemate.ui.screens.FavoritesScreen
import com.fplity.recitemate.ui.screens.HomeScreen
import com.fplity.recitemate.ui.screens.SettingsScreen
import com.fplity.recitemate.ui.theme.Cream
import com.fplity.recitemate.ui.theme.LeafGreen

object AppRoute {
    const val HOME = "home"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
    const val DETAIL = "detail/{articleId}"
    fun detail(articleId: Int) = "detail/$articleId"
}

@Composable
fun QingjianNavGraph(
    articles: List<Article>,
    loadError: Boolean,
    favorites: Set<Int>,
    fontSize: FontSize,
    readingTheme: ReadingTheme,
    onToggleFavorite: (Int) -> Unit,
    onFontSizeChange: (FontSize) -> Unit,
    onReadingThemeChange: (ReadingTheme) -> Unit
) {
    val navController = rememberNavController()
    Scaffold(
        containerColor = Cream,
        bottomBar = { FloatingBottomNavigation(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(AppRoute.HOME) {
                HomeScreen(
                    articles = articles,
                    loadError = loadError,
                    favorites = favorites,
                    onOpenArticle = { navController.navigate(AppRoute.detail(it)) },
                    onToggleFavorite = onToggleFavorite
                )
            }
            composable(AppRoute.FAVORITES) {
                FavoritesScreen(
                    articles = articles.filter { it.id in favorites },
                    favorites = favorites,
                    onOpenArticle = { navController.navigate(AppRoute.detail(it)) },
                    onToggleFavorite = onToggleFavorite
                )
            }
            composable(AppRoute.SETTINGS) {
                SettingsScreen(
                    fontSize = fontSize,
                    readingTheme = readingTheme,
                    onFontSizeChange = onFontSizeChange,
                    onReadingThemeChange = onReadingThemeChange
                )
            }
            composable(AppRoute.DETAIL) { entry ->
                val article = articles.firstOrNull { it.id == entry.arguments?.getString("articleId")?.toIntOrNull() }
                if (article != null) {
                    DetailScreen(
                        article = article,
                        isFavorite = article.id in favorites,
                        fontScale = fontSize.scale,
                        onBack = { navController.popBackStack() },
                        onToggleFavorite = { onToggleFavorite(article.id) }
                    )
                } else {
                    HomeScreen(articles, loadError, favorites, {}, onToggleFavorite)
                }
            }
        }
    }
}

private data class BottomDestination(val route: String, val label: String)

@Composable
private fun FloatingBottomNavigation(navController: NavHostController) {
    val destinations = listOf(
        BottomDestination(AppRoute.HOME, "学习"),
        BottomDestination(AppRoute.FAVORITES, "收藏"),
        BottomDestination(AppRoute.SETTINGS, "设置")
    )
    val current = navController.currentBackStackEntryAsState().value?.destination
    Surface(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp).clip(MaterialTheme.shapes.extraLarge),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        shadowElevation = 8.dp
    ) {
        NavigationBar(containerColor = androidx.compose.ui.graphics.Color.Transparent) {
            destinations.forEachIndexed { index, destination ->
                val selected = current?.hierarchy?.any { it.route == destination.route } == true
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(destination.route) {
                            popUpTo(AppRoute.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        val icon = when (index) {
                            0 -> Icons.Filled.Home
                            1 -> Icons.Filled.Favorite
                            else -> Icons.Filled.Settings
                        }
                        Icon(icon, contentDescription = destination.label)
                    },
                    label = { Text(destination.label) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = LeafGreen.copy(alpha = 0.25f))
                )
            }
        }
    }
}
