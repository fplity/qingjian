package com.fplity.recitemate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fplity.recitemate.data.model.Article
import com.fplity.recitemate.ui.theme.DeepGreen
import com.fplity.recitemate.ui.theme.Divider
import com.fplity.recitemate.ui.theme.MutedInk
import com.fplity.recitemate.ui.theme.WarmOrange

@Composable
fun DetailScreen(
    article: Article,
    isFavorite: Boolean,
    fontScale: Float,
    hasCompletedToday: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onComplete: () -> Unit
) {
    var showTranslation by rememberSaveable(article.id) { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = "返回"
                    onClick(label = "返回") { onBack(); true }
                }
            ) { Icon(Icons.Filled.ArrowBack, "返回") }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(article.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${article.dynasty} · ${article.author}", style = MaterialTheme.typography.labelMedium, color = MutedInk)
            }
            val favoriteLabel = if (isFavorite) "取消收藏" else "收藏"
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = favoriteLabel
                    onClick(label = favoriteLabel) { onToggleFavorite(); true }
                }
            ) {
                Icon(Icons.Filled.Bookmark, if (isFavorite) "取消收藏" else "收藏", tint = if (isFavorite) DeepGreen else MutedInk)
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ReadingTab("原文", !showTranslation, Modifier.weight(1f)) { showTranslation = false }
            ReadingTab("翻译", showTranslation, Modifier.weight(1f)) { showTranslation = true }
        }
        Box(Modifier.weight(1f)) {
            if (showTranslation) TranslationContent(article, fontScale) else OriginalContent(article, fontScale)
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
            Button(
                onClick = onComplete,
                enabled = !hasCompletedToday,
                modifier = Modifier.fillMaxWidth().testTag("detail_complete")
                    .clearAndSetSemantics {
                        contentDescription = "我已完成本次背诵"
                        if (hasCompletedToday) {
                            disabled()
                        } else {
                            onClick(label = "我已完成本次背诵") { onComplete(); true }
                        }
                    }
            ) { Text(if (hasCompletedToday) "今日已完成，可继续阅读" else "我已完成本次背诵") }
            Text(
                if (hasCompletedToday) "今天已记录一次学习完成，重复练习不会增加叶子。" else "请根据自己的学习情况确认，不进行自动背诵检测。",
                style = MaterialTheme.typography.bodySmall,
                color = MutedInk,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ReadingTab(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.clearAndSetSemantics {
            contentDescription = text
            onClick(label = text) { onClick(); true }
        },
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) DeepGreen else Color.Transparent,
            contentColor = if (selected) Color.White else DeepGreen
        )
    ) { Text(text) }
}

@Composable
private fun OriginalContent(article: Article, fontScale: Float) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 28.dp, end = 28.dp, top = 24.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { Text(article.category, color = WarmOrange, style = MaterialTheme.typography.labelLarge) }
        itemsIndexed(article.content) { _, line ->
            Text(line, fontSize = (20f * fontScale).sp, lineHeight = (35f * fontScale).sp, color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Start)
        }
    }
}

@Composable
private fun TranslationContent(article: Article, fontScale: Float) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp)
    ) {
        item { Text("白话翻译", color = WarmOrange, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 16.dp)) }
        itemsIndexed(article.translation) { index, paragraph ->
            Row(Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(22.dp)) {
                    androidx.compose.foundation.Canvas(Modifier.width(12.dp).height(12.dp)) { drawCircle(DeepGreen) }
                    if (index != article.translation.lastIndex) Spacer(Modifier.width(1.dp).height(50.dp).background(Divider))
                }
                Spacer(Modifier.width(12.dp))
                Text(paragraph, modifier = Modifier.weight(1f).padding(bottom = 26.dp), fontSize = (17f * fontScale).sp, lineHeight = (29f * fontScale).sp, color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}
