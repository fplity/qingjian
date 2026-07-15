package com.fplity.recitemate.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.School
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fplity.recitemate.data.local.FontSize
import com.fplity.recitemate.data.local.ReadingTheme
import com.fplity.recitemate.data.local.UserPreferences
import com.fplity.recitemate.data.model.Article
import com.fplity.recitemate.ui.screens.DetailScreen
import com.fplity.recitemate.ui.screens.FavoritesScreen
import com.fplity.recitemate.ui.screens.HomeScreen
import com.fplity.recitemate.ui.screens.PetGardenScreen
import com.fplity.recitemate.ui.screens.PracticeScreen
import com.fplity.recitemate.ui.screens.SettingsScreen
import com.fplity.recitemate.ui.theme.Cream
import com.fplity.recitemate.ui.theme.LeafGreen
import java.time.LocalDate

object AppRoute {
    const val HOME = "home"
    const val PRACTICE = "practice"
    const val GARDEN = "garden"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
    const val DETAIL = "detail/{articleId}"
    fun detail(articleId: Int) = "detail/$articleId"
}

@Composable
fun QingjianNavGraph(
    articles: List<Article>,
    loadError: Boolean,
    preferences: UserPreferences,
    fontSize: FontSize,
    readingTheme: ReadingTheme,
    onToggleFavorite: (Int) -> Unit,
    onCompleteRecitation: (Int) -> Unit,
    onFontSizeChange: (FontSize) -> Unit,
    onReadingThemeChange: (ReadingTheme) -> Unit
) {
    val navController = rememberNavController()
    val today = LocalDate.now()
    val hasCompletedToday = preferences.lastCompletedDate == today
    val dailyArticle = articles.getOrNull(Math.floorMod(today.toEpochDay(), articles.size.coerceAtLeast(1).toLong()).toInt())
    Scaffold(containerColor = Cream, bottomBar = { FloatingBottomNavigation(navController) }) { padding ->
        NavHost(navController = navController, startDestination = AppRoute.HOME, modifier = Modifier.padding(padding)) {
            composable(AppRoute.HOME) {
                HomeScreen(
                    articles = articles,
                    loadError = loadError,
                    favorites = preferences.favorites,
                    dailyArticle = dailyArticle,
                    leafTotal = preferences.leafTotal,
                    streak = preferences.currentStreak,
                    onOpenArticle = { navController.navigate(AppRoute.detail(it)) },
                    onToggleFavorite = onToggleFavorite
                )
            }
            composable(AppRoute.PRACTICE) {
                PracticeScreen(articles, hasCompletedToday, onCompleteRecitation)
            }
            composable(AppRoute.GARDEN) {
                PetGardenScreen(preferences.leafTotal, preferences.currentStreak, preferences.learnedArticleIds.size)
            }
            composable(AppRoute.FAVORITES) {
                FavoritesScreen(articles.filter { it.id in preferences.favorites }, preferences.favorites, { navController.navigate(AppRoute.detail(it)) }, onToggleFavorite)
            }
            composable(AppRoute.SETTINGS) {
                SettingsScreen(fontSize, readingTheme, onFontSizeChange, onReadingThemeChange)
            }
            composable(AppRoute.DETAIL) { entry ->
                val article = articles.firstOrNull { it.id == entry.arguments?.getString("articleId")?.toIntOrNull() }
                if (article != null) {
                    DetailScreen(
                        article = article,
                        isFavorite = article.id in preferences.favorites,
                        fontScale = fontSize.scale,
                        hasCompletedToday = hasCompletedToday,
                        onBack = { navController.popBackStack() },
                        onToggleFavorite = { onToggleFavorite(article.id) },
                        onComplete = { onCompleteRecitation(article.id) }
                    )
                } else {
                    HomeScreen(articles, loadError, preferences.favorites, dailyArticle, preferences.leafTotal, preferences.currentStreak, {}, onToggleFavorite)
                }
            }
        }
    }
}

private data class BottomDestination(val route: String, val label: String)

@Composable
private fun FloatingBottomNavigation(navController: NavHostController) {
    val destinations = listOf(
        BottomDestination(AppRoute.HOME, "浏览"),
        BottomDestination(AppRoute.PRACTICE, "学习"),
        BottomDestination(AppRoute.GARDEN, "小笺"),
        BottomDestination(AppRoute.FAVORITES, "收藏"),
        BottomDestination(AppRoute.SETTINGS, "设置")
    )
    val current by navController.currentBackStackEntryAsState()
    Surface(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).clip(MaterialTheme.shapes.extraLarge),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        shadowElevation = 8.dp
    ) {
        NavigationBar(containerColor = androidx.compose.ui.graphics.Color.Transparent) {
            destinations.forEachIndexed { index, destination ->
                val selected = current?.destination?.hierarchy?.any { it.route == destination.route } == true
                val openDestination = {
                    if (destination.route == AppRoute.HOME) {
                        navController.navigate(AppRoute.HOME) {
                            popUpTo(AppRoute.HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(destination.route) {
                            popUpTo(AppRoute.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
                NavigationBarItem(
                    selected = selected,
                    onClick = openDestination,
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = "切换到${destination.label}"
                        onClick(label = "切换到${destination.label}") {
                            openDestination()
                            true
                        }
                    },
                    icon = {
                        val icon = when (index) {
                            0 -> Icons.Filled.MenuBook
                            1 -> Icons.Filled.School
                            2 -> Icons.Filled.Park
                            3 -> Icons.Filled.Favorite
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
