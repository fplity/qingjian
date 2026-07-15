package com.fplity.recitemate.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.time.LocalDate
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
    val readingTheme: ReadingTheme = ReadingTheme.LIGHT,
    val leafTotal: Int = 0,
    val learnedArticleIds: Set<Int> = emptySet(),
    val lastCompletedDate: LocalDate? = null,
    val currentStreak: Int = 0,
    val todayCompletedArticleId: Int? = null
) {
    val petLevel: Int get() = MotivationRules.petLevelForLeaves(leafTotal)
}

/** Pure, date-injected rules keep reward behavior deterministic and unit-testable. */
data class CompletionProgress(
    val leafTotal: Int = 0,
    val learnedArticleIds: Set<Int> = emptySet(),
    val lastCompletedDate: LocalDate? = null,
    val currentStreak: Int = 0,
    val todayCompletedArticleId: Int? = null
)

data class CompletionAward(val progress: CompletionProgress, val awarded: Boolean)

object MotivationRules {
    private val levelThresholds = listOf(0, 5, 15, 30, 50)

    fun petLevelForLeaves(leaves: Int): Int = when {
        leaves >= levelThresholds[4] -> 5
        leaves >= levelThresholds[3] -> 4
        leaves >= levelThresholds[2] -> 3
        leaves >= levelThresholds[1] -> 2
        else -> 1
    }

    fun nextLevelThreshold(leaves: Int): Int? = levelThresholds.firstOrNull { it > leaves }

    fun awardCompletion(
        progress: CompletionProgress,
        articleId: Int,
        completedOn: LocalDate
    ): CompletionAward {
        if (progress.lastCompletedDate == completedOn) return CompletionAward(progress, awarded = false)

        val nextStreak = if (progress.lastCompletedDate?.plusDays(1) == completedOn) {
            progress.currentStreak + 1
        } else {
            1
        }
        return CompletionAward(
            progress.copy(
                leafTotal = progress.leafTotal + 1,
                learnedArticleIds = progress.learnedArticleIds + articleId,
                lastCompletedDate = completedOn,
                currentStreak = nextStreak,
                todayCompletedArticleId = articleId
            ),
            awarded = true
        )
    }
}

class PreferenceManager(private val context: Context) {
    private val favoriteIds = stringSetPreferencesKey("favorite_ids")
    private val fontSize = stringPreferencesKey("font_size")
    private val readingTheme = stringPreferencesKey("reading_theme")
    private val leafTotal = intPreferencesKey("leaf_total")
    private val learnedArticleIds = stringSetPreferencesKey("learned_article_ids")
    private val lastCompletedDate = stringPreferencesKey("last_completed_iso_date")
    private val currentStreak = intPreferencesKey("current_streak")
    private val todayCompletedArticleId = intPreferencesKey("today_completed_article_id")

    val preferences: Flow<UserPreferences> = context.qingjianDataStore.data.map { values ->
        UserPreferences(
            favorites = values[favoriteIds].orEmpty().mapNotNull { it.toIntOrNull() }.toSet(),
            fontSize = values[fontSize].asFontSize(),
            readingTheme = values[readingTheme].asReadingTheme(),
            leafTotal = values[leafTotal] ?: 0,
            learnedArticleIds = values[learnedArticleIds].orEmpty().mapNotNull { it.toIntOrNull() }.toSet(),
            lastCompletedDate = values[lastCompletedDate].asLocalDate(),
            currentStreak = values[currentStreak] ?: 0,
            todayCompletedArticleId = values[todayCompletedArticleId]
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

    /** Records an honest user-confirmed session. At most one leaf can be earned per local day. */
    suspend fun completeRecitation(articleId: Int, completedOn: LocalDate = LocalDate.now()): Boolean {
        var awarded = false
        context.qingjianDataStore.edit { values ->
            val current = CompletionProgress(
                leafTotal = values[leafTotal] ?: 0,
                learnedArticleIds = values[learnedArticleIds].orEmpty().mapNotNull { it.toIntOrNull() }.toSet(),
                lastCompletedDate = values[lastCompletedDate].asLocalDate(),
                currentStreak = values[currentStreak] ?: 0,
                todayCompletedArticleId = values[todayCompletedArticleId]
            )
            val result = MotivationRules.awardCompletion(current, articleId, completedOn)
            awarded = result.awarded
            if (result.awarded) {
                val updated = result.progress
                values[leafTotal] = updated.leafTotal
                values[learnedArticleIds] = updated.learnedArticleIds.map(Int::toString).toSet()
                values[lastCompletedDate] = completedOn.toString()
                values[currentStreak] = updated.currentStreak
                values[todayCompletedArticleId] = articleId
            }
        }
        return awarded
    }

    private fun String?.asFontSize() = runCatching { FontSize.valueOf(this.orEmpty()) }
        .getOrDefault(FontSize.STANDARD)

    private fun String?.asReadingTheme() = runCatching { ReadingTheme.valueOf(this.orEmpty()) }
        .getOrDefault(ReadingTheme.LIGHT)

    private fun String?.asLocalDate(): LocalDate? = runCatching { LocalDate.parse(this.orEmpty()) }.getOrNull()
}
