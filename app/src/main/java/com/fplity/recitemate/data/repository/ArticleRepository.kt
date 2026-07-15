package com.fplity.recitemate.data.repository

import android.content.Context
import com.fplity.recitemate.data.model.Article
import org.json.JSONArray

class ArticleRepository(private val context: Context) {
    fun loadArticles(): Result<List<Article>> = runCatching {
        context.assets.open("articles.json").bufferedReader().use { reader ->
            val array = JSONArray(reader.readText())
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val articleId = item.getInt("id")
                    val content = item.getJSONArray("content").toStrings()
                    val translations = item.getJSONArray("translation").toStrings()
                    add(
                        Article(
                            id = articleId,
                            title = item.getString("title"),
                            dynasty = item.getString("dynasty"),
                            author = item.getString("author"),
                            category = item.getString("category"),
                            content = content,
                            translation = translations,
                            sentences = ArticleAnnotationBuilder.build(articleId, content, translations)
                        )
                    )
                }
            }
        }
    }

    private fun JSONArray.toStrings(): List<String> = buildList {
        for (index in 0 until length()) add(getString(index))
    }
}
