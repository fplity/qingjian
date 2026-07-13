package com.fplity.recitemate.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.qingjianDataStore by preferencesDataStore(name = "qingjian_preferences")

enum class FontSize(val label: String, val scale: Float) {
    SMALL("小", 0.88f), STANDARD("标准", 1f), LARGE("大", 1.18f)
}

enum class ReadingTheme(val label: String) { LIGHT("浅色"), SYSTEM("跟随系统") }

data class UserPreferences(
    val favorites: Set<Int> = emptySet(),
    val fontSize: FontSize = FontSize.STANDARD,
    val readingTheme: ReadingTheme = ReadingTheme.LIGHT
)

class PreferenceManager(private val context: Context) {
    private val favoriteIds = stringSetPreferencesKey("favorite_ids")
    private val fontSize = stringPreferencesKey("font_size")
    private val readingTheme = stringPreferencesKey("reading_theme")

    val preferences: Flow<UserPreferences> = context.qingjianDataStore.data.map { values ->
        UserPreferences(
            favorites = values[favoriteIds].orEmpty().mapNotNull { it.toIntOrNull() }.toSet(),
            fontSize = values[fontSize].asFontSize(),
            readingTheme = values[readingTheme].asReadingTheme()
        )
    }

    suspend fun toggleFavorite(articleId: Int) {
        context.qingjianDataStore.edit { values ->
            val updated = values[favoriteIds].orEmpty().toMutableSet()
            if (!updated.add(articleId.toString())) updated.remove(articleId.toString())
            values[favoriteIds] = updated
        }
    }

    suspend fun setFontSize(value: FontSize) {
        context.qingjianDataStore.edit { it[fontSize] = value.name }
    }

    suspend fun setReadingTheme(value: ReadingTheme) {
        context.qingjianDataStore.edit { it[readingTheme] = value.name }
    }

    private fun String?.asFontSize() = runCatching { FontSize.valueOf(this.orEmpty()) }
        .getOrDefault(FontSize.STANDARD)

    private fun String?.asReadingTheme() = runCatching { ReadingTheme.valueOf(this.orEmpty()) }
        .getOrDefault(ReadingTheme.LIGHT)
}
