package com.fplity.recitemate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fplity.recitemate.data.model.Article
import com.fplity.recitemate.ui.components.EmptyState
import com.fplity.recitemate.ui.theme.DeepGreen
import com.fplity.recitemate.ui.theme.MutedInk
import com.fplity.recitemate.ui.theme.PaleGreen

@Composable
fun PracticeScreen(
    articles: List<Article>,
    hasCompletedToday: Boolean,
    onComplete: (Int) -> Unit
) {
    val article = remember(articles) { articles.randomOrNull() }
    var started by rememberSaveable { mutableStateOf(false) }
    var revealed by rememberSaveable { mutableStateOf(false) }

    if (article == null) {
        EmptyState("暂时没有可练习的篇目", "请先检查本地文章数据后再开始练习。")
        return
    }

    val nonBlankIndexes = remember(article.id, article.content) {
        article.content.mapIndexedNotNull { index, line -> index.takeIf { line.isNotBlank() } }
    }
    val hiddenIndex = remember(article.id) { nonBlankIndexes.randomOrNull() }

    if (hiddenIndex == null) {
        EmptyState("这篇文章暂时不能练习", "本地内容没有可供回忆的原文行。")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("学习", style = MaterialTheme.typography.headlineMedium, color = DeepGreen, fontWeight = FontWeight.Bold)
            Text("随机抽一篇，先在心中补全一句，再由你自己确认本次学习。", color = MutedInk, modifier = Modifier.padding(top = 5.dp))
        }
        if (!started) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = PaleGreen), shape = MaterialTheme.shapes.extraLarge) {
                    Column(Modifier.padding(20.dp)) {
                        Text("本次练习", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Text(article.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 10.dp))
                        Text("${article.dynasty} · ${article.author}", color = MutedInk, modifier = Modifier.padding(top = 4.dp))
                        Button(
                            onClick = { started = true },
                            modifier = Modifier.fillMaxWidth().padding(top = 18.dp).testTag("practice_start")
                                .clearAndSetSemantics {
                                    contentDescription = "开始本次练习"
                                    onClick(label = "开始本次练习") { started = true; true }
                                }
                        ) { Text("开始练习") }
                    }
                }
            }
        } else {
            item {
                Text(article.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("请先回忆被遮住的一句。", color = MutedInk, modifier = Modifier.padding(top = 4.dp))
            }
            itemsIndexed(article.content) { index, line ->
                if (index == hiddenIndex && !revealed) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PaleGreen),
                        modifier = Modifier.fillMaxWidth().testTag("practice_hidden_line")
                    ) {
                        Text("这一句已隐藏，请先在心中补全。", modifier = Modifier.padding(16.dp), color = DeepGreen)
                    }
                } else {
                    Text(line, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth().testTag("practice_line_$index"))
                }
            }
            item {
                if (!revealed) {
                    OutlinedButton(
                        onClick = { revealed = true },
                        modifier = Modifier.fillMaxWidth().testTag("practice_reveal")
                            .clearAndSetSemantics {
                                contentDescription = "查看原句"
                                onClick(label = "查看原句") { revealed = true; true }
                            }
                    ) { Text("查看原句") }
                }
                Button(
                    onClick = { onComplete(article.id) },
                    enabled = !hasCompletedToday,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp).testTag("practice_complete")
                        .clearAndSetSemantics {
                            contentDescription = "我已完成本次背诵"
                            if (hasCompletedToday) {
                                disabled()
                            } else {
                                onClick(label = "我已完成本次背诵") { onComplete(article.id); true }
                            }
                        }
                ) { Text(if (hasCompletedToday) "今日已完成，可继续练习" else "我已完成本次背诵") }
                Text(
                    if (hasCompletedToday) "今天的学习记录已保存；继续练习不会重复获得叶子。" else "请根据自己的学习情况确认，不进行自动背诵检测。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
