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
                    add(
                        Article(
                            id = item.getInt("id"),
                            title = item.getString("title"),
                            dynasty = item.getString("dynasty"),
                            author = item.getString("author"),
                            category = item.getString("category"),
                            content = item.getJSONArray("content").toStrings(),
                            translation = item.getJSONArray("translation").toStrings()
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
