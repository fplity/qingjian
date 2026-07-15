package com.fplity.recitemate

import com.fplity.recitemate.data.local.CompletionProgress
import com.fplity.recitemate.data.local.MotivationRules
import com.fplity.recitemate.data.local.PetInteraction
import com.fplity.recitemate.data.local.PetMood
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MotivationRulesTest {
    private val today = LocalDate.of(2026, 7, 15)

    @Test
    fun everyLevelBoundaryHasTheExpectedLevelAndNextThreshold() {
        val expected = mapOf(0 to 1, 4 to 1, 5 to 2, 14 to 2, 15 to 3, 29 to 3, 30 to 4, 49 to 4, 50 to 5)

        expected.forEach { (leaves, level) -> assertEquals("leaves=$leaves", level, MotivationRules.petLevelForLeaves(leaves)) }
        val nextThresholds = mapOf(0 to 5, 4 to 5, 5 to 15, 14 to 15, 15 to 30, 29 to 30, 30 to 50, 49 to 50, 50 to null)
        nextThresholds.forEach { (leaves, threshold) ->
            assertEquals("next threshold at $leaves leaves", threshold, MotivationRules.nextLevelThreshold(leaves))
        }
    }

    @Test
    fun newArticlesEarnLeavesOnTheSameDayButRepeatingOneDoesNot() {
        val first = MotivationRules.awardCompletion(CompletionProgress(), articleId = 7, completedOn = today)
        val second = MotivationRules.awardCompletion(first.progress, articleId = 8, completedOn = today)
        val repeated = MotivationRules.awardCompletion(second.progress, articleId = 7, completedOn = today)

        assertTrue(first.awarded)
        assertTrue(second.awarded)
        assertEquals(2, second.progress.leafTotal)
        assertEquals(1, second.progress.currentStreak)
        assertFalse(repeated.awarded)
        assertEquals(second.progress, repeated.progress)
        assertTrue(repeated.unlockedKeepsakes.isEmpty())
    }

    @Test
    fun streakDoesNotIncreaseTwiceTodayIncreasesTomorrowAndResetsAfterGap() {
        val first = MotivationRules.awardCompletion(CompletionProgress(), 1, today).progress
        val sameDay = MotivationRules.awardCompletion(first, 2, today).progress
        val adjacent = MotivationRules.awardCompletion(sameDay, 3, today.plusDays(1)).progress
        val afterGap = MotivationRules.awardCompletion(adjacent, 4, today.plusDays(3)).progress

        assertEquals(1, first.currentStreak)
        assertEquals(1, sameDay.currentStreak)
        assertEquals(2, adjacent.currentStreak)
        assertEquals(1, afterGap.currentStreak)
    }

    @Test
    fun moodsCoverTodayActiveStreakFirstVisitAndAGentleReturn() {
        assertEquals(PetMood.PROUD, MotivationRules.petMood(today, 4, today))
        assertEquals(PetMood.STEADY, MotivationRules.petMood(today.minusDays(1), 3, today))
        assertEquals(PetMood.CURIOUS, MotivationRules.petMood(null, 0, today))
        assertEquals(PetMood.WELCOME_BACK, MotivationRules.petMood(today.minusDays(3), 4, today))
    }

    @Test
    fun repliesAreDeterministicNonEmptyAndInteractionDoesNotGrantKeepsakes() {
        PetMood.entries.forEach { mood ->
            val first = MotivationRules.companionMessage(null, mood, today, interactionCount = 2)
            val second = MotivationRules.companionMessage(null, mood, today, interactionCount = 2)
            assertTrue(first.isNotBlank())
            assertEquals(first, second)
        }
        val greeting = MotivationRules.companionMessage(PetInteraction.GREETING, PetMood.CURIOUS, today, 1)
        val pat = MotivationRules.companionMessage(PetInteraction.HEAD_PAT, PetMood.CURIOUS, today, 1)
        assertTrue(greeting.isNotBlank())
        assertTrue(pat.isNotBlank())
        assertNotEquals(greeting, pat)
        assertEquals(null, MotivationRules.newlyUnlockedKeepsake(100, 14, emptySet()))
    }

    @Test
    fun firstLearningAndEveryLevelCrossingUnlockExactlyOnce() {
        val first = MotivationRules.awardCompletion(CompletionProgress(), 1, today)
        assertEquals(listOf("first_learning"), first.unlockedKeepsakes.map { it.id })

        mapOf(5 to "level_2", 15 to "level_3", 30 to "level_4", 50 to "level_5").forEach { (threshold, keepsakeId) ->
            val crossing = MotivationRules.awardCompletion(
                CompletionProgress(
                    leafTotal = threshold - 1,
                    learnedArticleIds = (1 until threshold).toSet(),
                    keepsakeIds = setOf("first_learning")
                ),
                articleId = threshold,
                completedOn = today
            )
            assertEquals(listOf(keepsakeId), crossing.unlockedKeepsakes.map { it.id })
            assertTrue(MotivationRules.milestoneKeepsakes(threshold - 1, threshold, 1, setOf(keepsakeId)).isEmpty())
        }
    }

    @Test
    fun everyStreakMilestoneUnlocksOnceAndKeepsakeDisplayOrderIsStable() {
        mapOf(3 to "streak_3", 7 to "streak_7", 14 to "streak_14").forEach { (streak, keepsakeId) ->
            val award = MotivationRules.awardCompletion(
                CompletionProgress(
                    leafTotal = 20,
                    learnedArticleIds = (1..20).toSet(),
                    lastCompletedDate = today.minusDays(1),
                    currentStreak = streak - 1
                ),
                articleId = 100 + streak,
                completedOn = today
            )
            assertEquals(listOf(keepsakeId), award.unlockedKeepsakes.map { it.id })
            assertTrue(MotivationRules.milestoneKeepsakes(20, 21, streak, setOf(keepsakeId)).isEmpty())
        }

        val duplicateMilestone = MotivationRules.milestoneKeepsakes(4, 5, 3, setOf("level_2", "streak_3"))
        assertTrue(duplicateMilestone.isEmpty())
        val ordered = MotivationRules.keepsakes.filter { it.id in setOf("first_learning", "level_2", "streak_3") }
        assertEquals(listOf("first_learning", "level_2", "streak_3"), ordered.map { it.id })
    }
}
