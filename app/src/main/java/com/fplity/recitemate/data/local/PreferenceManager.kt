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

enum class PetInteraction(val label: String) {
    GREETING("和小笺打招呼"),
    HEAD_PAT("轻轻摸摸头")
}

enum class PetMood(val label: String) {
    CURIOUS("期待相见"),
    PROUD("为你开心"),
    STEADY("默默陪伴"),
    WELCOME_BACK("温柔等候")
}

data class PetKeepsake(
    val id: String,
    val title: String,
    val description: String
)

data class UserPreferences(
    val favorites: Set<Int> = emptySet(),
    val fontSize: FontSize = FontSize.STANDARD,
    val readingTheme: ReadingTheme = ReadingTheme.LIGHT,
    val leafTotal: Int = 0,
    val learnedArticleIds: Set<Int> = emptySet(),
    val lastCompletedDate: LocalDate? = null,
    val currentStreak: Int = 0,
    val todayCompletedArticleId: Int? = null,
    val petLastVisitedDate: LocalDate? = null,
    val petInteractionCount: Int = 0,
    val petKeepsakeIds: Set<String> = emptySet()
) {
    val petLevel: Int get() = MotivationRules.petLevelForLeaves(leafTotal)
}

/** State used by pure completion rules. Dates are supplied by callers to keep rules testable. */
data class CompletionProgress(
    val leafTotal: Int = 0,
    val learnedArticleIds: Set<Int> = emptySet(),
    val lastCompletedDate: LocalDate? = null,
    val currentStreak: Int = 0,
    val todayCompletedArticleId: Int? = null,
    val keepsakeIds: Set<String> = emptySet()
)

data class CompletionAward(
    val progress: CompletionProgress,
    val awarded: Boolean,
    val unlockedKeepsakes: List<PetKeepsake> = emptyList()
)

/** Returned to learning screens after a confirmation, so feedback always reflects persisted reward rules. */
data class CompletionFeedback(
    val awarded: Boolean,
    val unlockedKeepsakes: List<PetKeepsake> = emptyList()
)

/** One source of truth for pet progress, milestones, mood, and deterministic companion replies. */
object MotivationRules {
    private val levelThresholds = listOf(0, 5, 15, 30, 50)

    val keepsakes = listOf(
        PetKeepsake("first_learning", "初见书签", "小笺把第一片成长叶压成了书签，留在你的书页里。"),
        PetKeepsake("level_2", "展叶丝带", "花园长出新叶时，小笺系上的一段青绿色丝带。"),
        PetKeepsake("level_3", "共读书灯", "一盏为并肩读书点亮的小灯。"),
        PetKeepsake("level_4", "守望花种", "小笺珍藏的花种，等下一次耐心浇灌。"),
        PetKeepsake("level_5", "满园信笺", "写着“我们已经走了很远”的一封小信。"),
        PetKeepsake("streak_3", "三日晨露", "连续三天学习后，叶尖收集的一颗晨露。"),
        PetKeepsake("streak_7", "并肩灯", "连续七天学习后点亮的一盏小灯。"),
        PetKeepsake("streak_14", "远行书签", "连续十四天相伴后，小笺送你的远行书签。")
    )

    fun petLevelForLeaves(leaves: Int): Int = when {
        leaves >= levelThresholds[4] -> 5
        leaves >= levelThresholds[3] -> 4
        leaves >= levelThresholds[2] -> 3
        leaves >= levelThresholds[1] -> 2
        else -> 1
    }

    fun nextLevelThreshold(leaves: Int): Int? = levelThresholds.firstOrNull { it > leaves }

    fun levelStartThreshold(leaves: Int): Int = levelThresholds.last { it <= leaves }

    fun petMood(lastCompletedDate: LocalDate?, streak: Int, today: LocalDate): PetMood = when {
        lastCompletedDate == today -> PetMood.PROUD
        lastCompletedDate != null && lastCompletedDate.isBefore(today.minusDays(1)) -> PetMood.WELCOME_BACK
        streak >= 3 -> PetMood.STEADY
        else -> PetMood.CURIOUS
    }

    fun companionMessage(
        interaction: PetInteraction?,
        mood: PetMood,
        day: LocalDate,
        interactionCount: Int
    ): String {
        val messages = when (interaction) {
            PetInteraction.GREETING -> listOf(
                "见到你真好。今天想从哪一篇开始？",
                "我把书页压平了，等你一起读。",
                "慢一点也没关系，我们一起往前。"
            )
            PetInteraction.HEAD_PAT -> listOf(
                "嗯，收到你的鼓励了。",
                "我会把这份好心情留在今天。",
                "那我也给你一点勇气。"
            )
            null -> when (mood) {
                PetMood.PROUD -> listOf("今天的努力我都看见了，真厉害。", "这一页已经被你读亮啦。")
                PetMood.STEADY -> listOf("我们已经并肩走了好几天。", "不必赶路，持续翻页就很好。")
                PetMood.WELCOME_BACK -> listOf("欢迎回来，书一直替你留着。", "不用补昨天的遗憾，今天见面就很好。")
                PetMood.CURIOUS -> listOf("我在花园里读到一句很好的话，想和你分享。", "今天也一起收集一点新的光吧。")
            }
        }
        return messages[Math.floorMod(day.dayOfYear + interactionCount, messages.size)]
    }

    /** Interaction can rotate a reply but can never grant a learning milestone. */
    fun newlyUnlockedKeepsake(
        interactionCount: Int,
        streak: Int,
        ownedIds: Set<String>
    ): PetKeepsake? = null

    fun milestoneKeepsakes(
        previousLeaves: Int,
        newLeaves: Int,
        streak: Int,
        ownedIds: Set<String>
    ): List<PetKeepsake> {
        val eligibleIds = buildList {
            if (previousLeaves == 0 && newLeaves > 0) add("first_learning")
            levelThresholds.drop(1).forEachIndexed { index, threshold ->
                if (previousLeaves < threshold && newLeaves >= threshold) add("level_${index + 2}")
            }
            when (streak) {
                3 -> add("streak_3")
                7 -> add("streak_7")
                14 -> add("streak_14")
            }
        }
        return keepsakes.filter { it.id in eligibleIds && it.id !in ownedIds }
    }

    fun awardCompletion(
        progress: CompletionProgress,
        articleId: Int,
        completedOn: LocalDate
    ): CompletionAward {
        if (articleId in progress.learnedArticleIds) return CompletionAward(progress, awarded = false)

        val nextStreak = if (progress.lastCompletedDate == completedOn) {
            progress.currentStreak
        } else if (progress.lastCompletedDate?.plusDays(1) == completedOn) {
            progress.currentStreak + 1
        } else {
            1
        }
        val nextLeaves = progress.leafTotal + 1
        val unlocked = milestoneKeepsakes(progress.leafTotal, nextLeaves, nextStreak, progress.keepsakeIds)
        return CompletionAward(
            progress = progress.copy(
                leafTotal = nextLeaves,
                learnedArticleIds = progress.learnedArticleIds + articleId,
                lastCompletedDate = completedOn,
                currentStreak = nextStreak,
                todayCompletedArticleId = articleId,
                keepsakeIds = progress.keepsakeIds + unlocked.map(PetKeepsake::id)
            ),
            awarded = true,
            unlockedKeepsakes = unlocked
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
    private val petLastVisitedDate = stringPreferencesKey("pet_last_visited_iso_date")
    private val petInteractionCount = intPreferencesKey("pet_interaction_count")
    private val petKeepsakeIds = stringSetPreferencesKey("pet_keepsake_ids")

    val preferences: Flow<UserPreferences> = context.qingjianDataStore.data.map { values ->
        UserPreferences(
            favorites = values[favoriteIds].orEmpty().mapNotNull { it.toIntOrNull() }.toSet(),
            fontSize = values[fontSize].asFontSize(),
            readingTheme = values[readingTheme].asReadingTheme(),
            leafTotal = values[leafTotal] ?: 0,
            learnedArticleIds = values[learnedArticleIds].orEmpty().mapNotNull { it.toIntOrNull() }.toSet(),
            lastCompletedDate = values[lastCompletedDate].asLocalDate(),
            currentStreak = values[currentStreak] ?: 0,
            todayCompletedArticleId = values[todayCompletedArticleId],
            petLastVisitedDate = values[petLastVisitedDate].asLocalDate(),
            petInteractionCount = values[petInteractionCount] ?: 0,
            petKeepsakeIds = values[petKeepsakeIds].orEmpty()
        )
    }

    suspend fun toggleFavorite(articleId: Int) {
        context.qingjianDataStore.edit { values ->
            val updated = values[favoriteIds].orEmpty().toMutableSet()
            if (!updated.add(articleId.toString())) updated.remove(articleId.toString())
            values[favoriteIds] = updated
        }
    }

    suspend fun setFontSize(value: FontSize) { context.qingjianDataStore.edit { it[fontSize] = value.name } }

    suspend fun setReadingTheme(value: ReadingTheme) { context.qingjianDataStore.edit { it[readingTheme] = value.name } }

    /** Records an honest user-confirmed session. Every article can grow the garden only once. */
    suspend fun completeRecitation(articleId: Int, completedOn: LocalDate = LocalDate.now()): CompletionFeedback {
        var feedback = CompletionFeedback(awarded = false)
        context.qingjianDataStore.edit { values ->
            val current = CompletionProgress(
                leafTotal = values[leafTotal] ?: 0,
                learnedArticleIds = values[learnedArticleIds].orEmpty().mapNotNull { it.toIntOrNull() }.toSet(),
                lastCompletedDate = values[lastCompletedDate].asLocalDate(),
                currentStreak = values[currentStreak] ?: 0,
                todayCompletedArticleId = values[todayCompletedArticleId],
                keepsakeIds = values[petKeepsakeIds].orEmpty()
            )
            val result = MotivationRules.awardCompletion(current, articleId, completedOn)
            feedback = CompletionFeedback(result.awarded, result.unlockedKeepsakes)
            if (result.awarded) {
                val updated = result.progress
                values[leafTotal] = updated.leafTotal
                values[learnedArticleIds] = updated.learnedArticleIds.map(Int::toString).toSet()
                values[lastCompletedDate] = completedOn.toString()
                values[currentStreak] = updated.currentStreak
                values[todayCompletedArticleId] = articleId
                values[petKeepsakeIds] = updated.keepsakeIds
            }
        }
        return feedback
    }

    suspend fun visitPet(visitedOn: LocalDate = LocalDate.now()) {
        context.qingjianDataStore.edit { values -> values[petLastVisitedDate] = visitedOn.toString() }
    }

    suspend fun interactWithPet(interaction: PetInteraction, interactedOn: LocalDate = LocalDate.now()) {
        context.qingjianDataStore.edit { values ->
            values[petInteractionCount] = (values[petInteractionCount] ?: 0) + 1
            values[petLastVisitedDate] = interactedOn.toString()
        }
    }

    private fun String?.asFontSize() = runCatching { FontSize.valueOf(this.orEmpty()) }.getOrDefault(FontSize.STANDARD)
    private fun String?.asReadingTheme() = runCatching { ReadingTheme.valueOf(this.orEmpty()) }.getOrDefault(ReadingTheme.LIGHT)
    private fun String?.asLocalDate(): LocalDate? = runCatching { LocalDate.parse(this.orEmpty()) }.getOrNull()
}
