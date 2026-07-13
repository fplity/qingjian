package com.fplity.recitemate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fplity.recitemate.data.model.Article
import com.fplity.recitemate.ui.components.ArticleCard
import com.fplity.recitemate.ui.components.EmptyState
import com.fplity.recitemate.ui.theme.DeepGreen
import com.fplity.recitemate.ui.theme.MutedInk

@Composable
fun FavoritesScreen(
    articles: List<Article>,
    favorites: Set<Int>,
    onOpenArticle: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("我的收藏", style = MaterialTheme.typography.headlineMedium, color = DeepGreen, fontWeight = FontWeight.Bold)
            Text("留住想再读一遍的篇章", style = MaterialTheme.typography.bodyMedium, color = MutedInk, modifier = Modifier.padding(top = 5.dp))
        }
        if (articles.isEmpty()) {
            item { EmptyState("暂无收藏文章", "点击文章右上角书签，即可收藏篇目。") }
        } else {
            items(articles, key = { it.id }) { article ->
                ArticleCard(article, article.id in favorites, { onOpenArticle(article.id) }) { onToggleFavorite(article.id) }
            }
        }
    }
}
