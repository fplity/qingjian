package com.fplity.recitemate.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.fplity.recitemate.data.local.CompletionFeedback
import com.fplity.recitemate.data.model.AnnotatedSentence
import com.fplity.recitemate.data.model.AnnotatedWord
import com.fplity.recitemate.data.model.Article
import com.fplity.recitemate.ui.theme.DeepGreen
import com.fplity.recitemate.ui.theme.MutedInk
import com.fplity.recitemate.ui.theme.WarmOrange
import kotlin.math.roundToInt

@Composable
fun DetailScreen(
    article: Article,
    isFavorite: Boolean,
    fontScale: Float,
    hasCompletedArticle: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onComplete: ((CompletionFeedback) -> Unit) -> Unit
) {
    var showTranslation by rememberSaveable(article.id) { mutableStateOf(false) }
    var explanation by remember(article.id) { mutableStateOf<ExplanationSelection?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Header(article, isFavorite, onBack, onToggleFavorite)
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReadingTab("原文", !showTranslation, Modifier.weight(1f)) {
                    showTranslation = false
                    explanation = null
                }
                ReadingTab("翻译", showTranslation, Modifier.weight(1f)) {
                    showTranslation = true
                    explanation = null
                }
            }
            if (showTranslation) {
                TranslationContent(article, fontScale, Modifier.weight(1f))
            } else {
                OriginalContent(
                    article = article,
                    fontScale = fontScale,
                    modifier = Modifier.weight(1f),
                    onWordSelected = { word, anchor -> explanation = ExplanationSelection.Word(word, anchor) },
                    onSentenceSelected = { sentence, anchor -> explanation = ExplanationSelection.Sentence(sentence, anchor) }
                )
            }
            CompletionPanel(article.id, hasCompletedArticle, onComplete)
        }
        explanation?.let { selection ->
            AnchoredExplanationPopup(selection, onDismiss = { explanation = null })
        }
    }
}

@Composable
private fun Header(article: Article, isFavorite: Boolean, onBack: () -> Unit, onToggleFavorite: () -> Unit) {
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
            Text(article.dynasty + " · " + article.author, style = MaterialTheme.typography.labelMedium, color = MutedInk)
        }
        val favoriteLabel = if (isFavorite) "取消收藏" else "收藏"
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.clearAndSetSemantics {
                contentDescription = favoriteLabel
                onClick(label = favoriteLabel) { onToggleFavorite(); true }
            }
        ) { Icon(Icons.Filled.Bookmark, favoriteLabel, tint = if (isFavorite) DeepGreen else MutedInk) }
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
private fun OriginalContent(
    article: Article,
    fontScale: Float,
    modifier: Modifier,
    onWordSelected: (AnnotatedWord, IntRect) -> Unit,
    onSentenceSelected: (AnnotatedSentence, IntRect) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 28.dp, end = 28.dp, top = 24.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { Text(article.category, color = WarmOrange, style = MaterialTheme.typography.labelLarge) }
        items(article.sentences, key = { it.id }) { sentence ->
            InteractiveSentence(sentence, fontScale, onWordSelected, onSentenceSelected)
        }
    }
}

@Composable
private fun InteractiveSentence(
    sentence: AnnotatedSentence,
    fontScale: Float,
    onWordSelected: (AnnotatedWord, IntRect) -> Unit,
    onSentenceSelected: (AnnotatedSentence, IntRect) -> Unit
) {
    var layout by remember(sentence.id) { mutableStateOf<TextLayoutResult?>(null) }
    var textOrigin by remember(sentence.id) { mutableStateOf(Offset.Zero) }
    var sentenceBounds by remember(sentence.id) { mutableStateOf(IntRect.Zero) }
    Text(
        text = sentence.source,
        fontSize = (20f * fontScale).sp,
        lineHeight = (35f * fontScale).sp,
        textAlign = TextAlign.Start,
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                textOrigin = coordinates.positionInRoot()
                sentenceBounds = coordinates.boundsInRoot().toIntRect()
            }
            .pointerInput(sentence.id, layout, textOrigin, sentenceBounds) {
                detectTapGestures(
                    onTap = { position ->
                        val currentLayout = layout ?: return@detectTapGestures
                        val offset = currentLayout.getOffsetForPosition(position)
                        sentence.words.firstOrNull { offset in it.start until it.endExclusive }?.let { word ->
                            onWordSelected(word, currentLayout.getBoundingBox(word.start).toRootRect(textOrigin))
                        }
                    },
                    onLongPress = { onSentenceSelected(sentence, sentenceBounds) }
                )
            },
        onTextLayout = { layout = it }
    )
}

@Composable
private fun TranslationContent(article: Article, fontScale: Float, modifier: Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 28.dp, end = 28.dp, top = 24.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("白话翻译", color = WarmOrange, style = MaterialTheme.typography.labelLarge) }
        items(article.sentences, key = { it.translationId }) { sentence ->
            Text(
                text = sentence.translation,
                fontSize = (17f * fontScale).sp,
                lineHeight = (29f * fontScale).sp
            )
        }
    }
}

@Composable
private fun CompletionPanel(
    articleId: Int,
    hasCompletedArticle: Boolean,
    onComplete: ((CompletionFeedback) -> Unit) -> Unit
) {
    var completionFeedback by remember(articleId) { mutableStateOf<CompletionFeedback?>(null) }
    var isCompleting by remember(articleId) { mutableStateOf(false) }
    val complete = {
        if (!isCompleting) {
            isCompleting = true
            onComplete { feedback ->
                if (feedback.awarded || completionFeedback?.awarded != true) {
                    completionFeedback = feedback
                }
                isCompleting = false
            }
        }
    }

    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
        Button(
            onClick = complete,
            enabled = !hasCompletedArticle && !isCompleting,
            modifier = Modifier.fillMaxWidth().clearAndSetSemantics {
                contentDescription = "我已完成本次背诵"
                if (hasCompletedArticle || isCompleting) {
                    disabled()
                } else {
                    onClick(label = "我已完成本次背诵") { complete(); true }
                }
            }
        ) { Text(if (hasCompletedArticle) "本篇已收录，可继续阅读" else "我已完成本次背诵") }
        Text(
            if (hasCompletedArticle) "这篇已收录进成长记录，重复练习不会增加叶子。"
            else "完成新篇目后，小笺的花园会长出一片叶子。",
            style = MaterialTheme.typography.bodySmall,
            color = MutedInk,
            modifier = Modifier.padding(top = 4.dp)
        )
        AnimatedVisibility(completionFeedback?.awarded == true) {
            Column(Modifier.padding(top = 8.dp)) {
                Text("小笺收到了这次努力，花园里多了一片新叶。", color = DeepGreen, fontWeight = FontWeight.SemiBold)
                completionFeedback?.unlockedKeepsakes?.forEach { keepsake ->
                    Text(
                        "小惊喜：${keepsake.title} · ${keepsake.description}",
                        color = DeepGreen,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

private sealed interface ExplanationSelection {
    val anchor: IntRect

    data class Word(val word: AnnotatedWord, override val anchor: IntRect) : ExplanationSelection
    data class Sentence(val sentence: AnnotatedSentence, override val anchor: IntRect) : ExplanationSelection
}

@Composable
private fun AnchoredExplanationPopup(selection: ExplanationSelection, onDismiss: () -> Unit) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val popupWidth = with(density) { 280.dp.roundToPx() }
    val popupHeight = with(density) { 156.dp.roundToPx() }
    val margin = with(density) { 12.dp.roundToPx() }
    val gap = with(density) { 8.dp.roundToPx() }
    val screenWidth = with(density) { configuration.screenWidthDp.dp.roundToPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.roundToPx() }
    val x = selection.anchor.left.coerceIn(margin, (screenWidth - popupWidth - margin).coerceAtLeast(margin))
    val below = selection.anchor.bottom + gap
    val y = if (below + popupHeight <= screenHeight - margin) below
    else (selection.anchor.top - popupHeight - gap).coerceAtLeast(margin)

    Popup(
        alignment = Alignment.TopStart,
        offset = IntOffset(x, y),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true, dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 8.dp,
            modifier = Modifier.width(280.dp)
        ) {
            Column(Modifier.padding(14.dp)) {
                when (selection) {
                    is ExplanationSelection.Word -> {
                        Text("字词释义", style = MaterialTheme.typography.labelLarge, color = DeepGreen)
                        Text(selection.word.source, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                        Text(selection.word.gloss, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                    }
                    is ExplanationSelection.Sentence -> {
                        Text("整句翻译", style = MaterialTheme.typography.labelLarge, color = DeepGreen)
                        Text(selection.sentence.translation, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.geometry.Rect.toIntRect(): IntRect = IntRect(
    left = left.roundToInt(),
    top = top.roundToInt(),
    right = right.roundToInt(),
    bottom = bottom.roundToInt()
)

private fun androidx.compose.ui.geometry.Rect.toRootRect(origin: Offset): IntRect = IntRect(
    left = (origin.x + left).roundToInt(),
    top = (origin.y + top).roundToInt(),
    right = (origin.x + right).roundToInt(),
    bottom = (origin.y + bottom).roundToInt()
)
