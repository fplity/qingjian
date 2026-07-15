package com.fplity.recitemate

import com.fplity.recitemate.data.local.CompletionProgress
import com.fplity.recitemate.data.local.MotivationRules
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MotivationRulesTest {
    @Test
    fun rewardIsIdempotentForTheSameCalendarDay() {
        val date = LocalDate.of(2026, 7, 15)
        val first = MotivationRules.awardCompletion(CompletionProgress(), articleId = 7, completedOn = date)
        val repeated = MotivationRules.awardCompletion(first.progress, articleId = 7, completedOn = date)

        assertTrue(first.awarded)
        assertEquals(1, first.progress.leafTotal)
        assertEquals(setOf(7), first.progress.learnedArticleIds)
        assertFalse(repeated.awarded)
        assertEquals(first.progress, repeated.progress)
    }

    @Test
    fun streakIncrementsOnlyOnTheNextCalendarDayAndResetsAfterGap() {
        val dayOne = LocalDate.of(2026, 7, 10)
        val first = MotivationRules.awardCompletion(CompletionProgress(), 1, dayOne).progress
        val consecutive = MotivationRules.awardCompletion(first, 2, dayOne.plusDays(1)).progress
        val afterGap = MotivationRules.awardCompletion(consecutive, 3, dayOne.plusDays(3)).progress

        assertEquals(1, first.currentStreak)
        assertEquals(2, consecutive.currentStreak)
        assertEquals(1, afterGap.currentStreak)
    }

    @Test
    fun petLevelsMatchEveryLeafThreshold() {
        assertEquals(1, MotivationRules.petLevelForLeaves(0))
        assertEquals(1, MotivationRules.petLevelForLeaves(4))
        assertEquals(2, MotivationRules.petLevelForLeaves(5))
        assertEquals(3, MotivationRules.petLevelForLeaves(15))
        assertEquals(4, MotivationRules.petLevelForLeaves(30))
        assertEquals(5, MotivationRules.petLevelForLeaves(50))
    }
}
