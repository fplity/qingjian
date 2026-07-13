package com.fplity.recitemate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fplity.recitemate.data.model.Article
import com.fplity.recitemate.ui.components.ArticleCard
import com.fplity.recitemate.ui.components.EmptyState
import com.fplity.recitemate.ui.theme.DeepGreen
import com.fplity.recitemate.ui.theme.MutedInk

@Composable
fun HomeScreen(
    articles: List<Article>,
    loadError: Boolean,
    favorites: Set<Int>,
    onOpenArticle: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit
) {
    var category by rememberSaveable { mutableStateOf("全部") }
    val shown = when (category) {
        "古诗词", "文言文" -> articles.filter { it.category == category }
        else -> articles
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text("青简", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = DeepGreen)
                Text("高中古诗文学习", style = MaterialTheme.typography.bodyLarge, color = MutedInk, modifier = Modifier.padding(top = 4.dp))
                Text("今日读几行古文，让心安静下来。", style = MaterialTheme.typography.bodyMedium, color = MutedInk, modifier = Modifier.padding(top = 18.dp))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("全部", "古诗词", "文言文").forEach { label ->
                    FilterChip(
                        selected = category == label,
                        onClick = { category = label },
                        label = { Text(label) }
                    )
                }
            }
        }
        if (shown.isEmpty()) {
            item {
                EmptyState(
                    title = if (loadError) "文章暂时无法读取" else "这里还没有文章",
                    description = if (loadError) "请检查本地文章数据后重试。" else "切换分类，或稍后再来看看。"
                )
            }
        } else {
            items(shown, key = { it.id }) { article ->
                ArticleCard(article, article.id in favorites, { onOpenArticle(article.id) }) { onToggleFavorite(article.id) }
            }
        }
    }
}
