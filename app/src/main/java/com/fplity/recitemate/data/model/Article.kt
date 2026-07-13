package com.fplity.recitemate.data.model

data class Article(
    val id: Int,
    val title: String,
    val dynasty: String,
    val author: String,
    val category: String,
    val content: List<String>,
    val translation: List<String>
)
