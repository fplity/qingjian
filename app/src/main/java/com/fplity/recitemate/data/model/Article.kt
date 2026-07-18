package com.fplity.recitemate.data.model

data class Article(
    val id: Int,
    val title: String,
    val dynasty: String,
    val author: String,
    val category: String,
    val content: List<String>,
    val translation: List<String>,
    val sentences: List<AnnotatedSentence> = emptyList()
)

/** A source sentence and its paired explanation record. */
data class AnnotatedSentence(
    val id: String,
    val translationId: String,
    val source: String,
    val translation: String,
    val words: List<AnnotatedWord>
)

/** A tap target in the source sentence. Positions are offsets in [AnnotatedSentence.source]. */
data class AnnotatedWord(
    val id: String,
    val glossId: String,
    val source: String,
    val start: Int,
    val endExclusive: Int,
    val gloss: String
)
