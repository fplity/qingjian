package com.fplity.recitemate.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleAnnotationBuilderTest {
    @Test
    fun `every sentence and source word receives a stable paired id`() {
        val sentences = ArticleAnnotationBuilder.build(
            articleId = 1,
            content = listOf("君子喻于义，小人喻于利。"),
            translations = listOf("君子重视道义，小人重视利益。")
        )

        assertEquals(2, sentences.size)
        assertEquals(listOf("article-1-sentence-1", "article-1-sentence-2"), sentences.map { it.id })
        assertEquals(
            listOf("article-1-translation-1", "article-1-translation-2"),
            sentences.map { it.translationId }
        )
        assertTrue(sentences.all { it.hasDirectTranslation })
        assertTrue(sentences.flatMap { it.words }.all { word ->
            word.id.isNotBlank() && word.glossId.isNotBlank() &&
                word.start < word.endExclusive && word.gloss.isNotBlank()
        })
    }

    @Test
    fun `longest matching term takes precedence over its individual characters`() {
        val sentence = ArticleAnnotationBuilder.build(
            articleId = 1,
            content = listOf("君子见贤思齐焉。"),
            translations = listOf("君子看见贤者便向他看齐。")
        ).single()

        assertTrue(sentence.words.any { it.source == "君子" })
        assertTrue(sentence.words.any { it.source == "见贤思齐" })
        assertEquals(2, sentence.words.first { it.source == "见贤思齐" }.start)
    }
}
