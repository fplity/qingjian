package com.fplity.recitemate.data.model

data class Article(
    val id: Int,
    val title: String,
    val dynasty: String,
    val author: String,
    val category: String,
    val content: List<String>,
    val translation: List<String>,
    /**
     * Source-of-truth annotations used by the reading screen.  IDs are stable
     * across app launches, so UI events never have to guess from display text.
     */
    val sentences: List<AnnotatedSentence> = emptyList()
)

data class AnnotatedSentence(
    val id: String,
    val translationId: String,
    val source: String,
    val translation: String,
    val hasDirectTranslation: Boolean,
    val words: List<AnnotatedWord>
)

data class AnnotatedWord(
    val id: String,
    val glossId: String,
    val source: String,
    val start: Int,
    val endExclusive: Int,
    val gloss: String
)
