package com.fplity.recitemate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.fplity.recitemate.data.local.PreferenceManager
import com.fplity.recitemate.data.local.ReadingTheme
import com.fplity.recitemate.data.local.UserPreferences
import com.fplity.recitemate.data.repository.ArticleRepository
import com.fplity.recitemate.navigation.QingjianNavGraph
import com.fplity.recitemate.ui.theme.ReciteMateTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { QingjianApp() }
    }
}

@androidx.compose.runtime.Composable
private fun QingjianApp() {
    val context = LocalContext.current.applicationContext
    val repository = remember(context) { ArticleRepository(context) }
    val preferenceManager = remember(context) { PreferenceManager(context) }
    val articleResult = remember(repository) { repository.loadArticles() }
    val preferences by preferenceManager.preferences.collectAsState(initial = UserPreferences())
    val scope = rememberCoroutineScope()
    val darkTheme = preferences.readingTheme == ReadingTheme.SYSTEM && isSystemInDarkTheme()

    ReciteMateTheme(darkTheme = darkTheme) {
        QingjianNavGraph(
            articles = articleResult.getOrDefault(emptyList()),
            loadError = articleResult.isFailure,
            favorites = preferences.favorites,
            fontSize = preferences.fontSize,
            readingTheme = preferences.readingTheme,
            onToggleFavorite = { articleId -> scope.launch { preferenceManager.toggleFavorite(articleId) } },
            onFontSizeChange = { value -> scope.launch { preferenceManager.setFontSize(value) } },
            onReadingThemeChange = { value -> scope.launch { preferenceManager.setReadingTheme(value) } }
        )
    }
}
