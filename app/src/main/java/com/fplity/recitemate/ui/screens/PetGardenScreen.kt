package com.fplity.recitemate.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fplity.recitemate.R
import com.fplity.recitemate.data.local.MotivationRules
import com.fplity.recitemate.data.local.PetInteraction
import com.fplity.recitemate.data.local.PetKeepsake
import com.fplity.recitemate.data.local.UserPreferences
import com.fplity.recitemate.ui.theme.DeepGreen
import com.fplity.recitemate.ui.theme.MutedInk
import com.fplity.recitemate.ui.theme.PaleGreen
import java.time.LocalDate
import kotlinx.coroutines.delay

/**
 * Xiaojian's garden deliberately keeps one approved portrait at every stage. The visual growth
 * comes from the hand-drawn-in-Compose garden layers, so a level never swaps in an unreviewed
 * character face.
 */
@Composable
fun PetGardenScreen(
    preferences: UserPreferences,
    onVisit: () -> Unit,
    onInteract: (PetInteraction) -> Unit,
    onOpenPractice: () -> Unit
) {
    val today = LocalDate.now()
    val level = MotivationRules.petLevelForLeaves(preferences.leafTotal)
    val levelStart = MotivationRules.levelStartThreshold(preferences.leafTotal)
    val nextThreshold = MotivationRules.nextLevelThreshold(preferences.leafTotal)
    val progress = if (nextThreshold == null) 1f else {
        (preferences.leafTotal - levelStart).toFloat() / (nextThreshold - levelStart).toFloat()
    }
    val mood = MotivationRules.petMood(preferences.lastCompletedDate, preferences.currentStreak, today)
    var message by remember(preferences.petInteractionCount, mood, today) {
        mutableStateOf(MotivationRules.companionMessage(null, mood, today, preferences.petInteractionCount))
    }
    var reacting by remember { mutableStateOf(false) }

    val transition = rememberInfiniteTransition(label = "xiaojian_idle_motion")
    val breathingScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.018f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "xiaojian_breathe"
    )
    val gardenSway by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "garden_sway"
    )
    val reactionScale by animateFloatAsState(
        targetValue = if (reacting) 1.065f else breathingScale,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "xiaojian_tap_scale"
    )
    val reactionRotation by animateFloatAsState(
        targetValue = if (reacting) -2.4f else 0f,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "xiaojian_tap_tilt"
    )

    LaunchedEffect(Unit) { onVisit() }
    LaunchedEffect(reacting) {
        if (reacting) {
            delay(300)
            reacting = false
        }
    }

    fun respond(interaction: PetInteraction) {
        // This counter only rotates dialogue. Learning leaves and keepsakes are completion-only.
        val nextInteraction = preferences.petInteractionCount + 1
        message = MotivationRules.companionMessage(interaction, mood, today, nextInteraction)
        reacting = true
        onInteract(interaction)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("小笺的花园", style = MaterialTheme.typography.headlineMedium, color = DeepGreen, fontWeight = FontWeight.Bold)
            Text("每完成一篇新的篇目，花园都会和她一起长大。", color = MutedInk, modifier = Modifier.padding(top = 5.dp))
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PaleGreen),
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.fillMaxWidth().testTag("pet_status").semantics {
                    contentDescription = "小笺等级 $level，${stageTitle(level)}，已有 ${preferences.leafTotal} 片成长叶，${mood.label}"
                }
            ) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stageTitle(level), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(stageNarrative(level), style = MaterialTheme.typography.bodyMedium, color = MutedInk, modifier = Modifier.padding(top = 3.dp))
                    Text(mood.label, style = MaterialTheme.typography.labelLarge, color = DeepGreen, modifier = Modifier.padding(top = 5.dp))
                    PetStageArtwork(
                        level = level,
                        scale = reactionScale,
                        rotation = reactionRotation,
                        sway = gardenSway,
                        reacting = reacting,
                        onClick = { respond(PetInteraction.HEAD_PAT) }
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)),
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                    ) {
                        Text("“$message”", color = DeepGreen, modifier = Modifier.padding(14.dp).testTag("pet_dialogue"))
                    }
                    Text(
                        "等级 $level · ${preferences.leafTotal} 片成长叶",
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepGreen,
                        modifier = Modifier.padding(top = 14.dp).testTag("pet_level")
                    )
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                    )
                    Text(
                        nextThreshold?.let { "再收集 ${it - preferences.leafTotal} 片叶子，解锁下一段花园。" }
                            ?: "小笺已经陪你走到花园最盛开的时刻。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedInk,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { respond(PetInteraction.GREETING) },
                    modifier = Modifier.weight(1f).testTag("pet_greeting").semantics { contentDescription = "和小笺打招呼" }
                ) {
                    Icon(Icons.Filled.WavingHand, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(" 打个招呼")
                }
                OutlinedButton(
                    onClick = { respond(PetInteraction.HEAD_PAT) },
                    modifier = Modifier.weight(1f).testTag("pet_pat").semantics { contentDescription = "轻轻摸摸小笺的头" }
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(" 摸摸头")
                }
            }
        }
        item {
            Button(
                onClick = onOpenPractice,
                modifier = Modifier.fillMaxWidth().testTag("pet_study_together").semantics { contentDescription = "和小笺一起复习，前往练习" }
            ) {
                Icon(Icons.Filled.MenuBook, contentDescription = null)
                Text(" 和小笺一起复习")
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GardenMetric("连续学习", "${preferences.currentStreak} 天", Modifier.weight(1f).testTag("streak_total"))
                GardenMetric("已学篇目", "${preferences.learnedArticleIds.size} 篇", Modifier.weight(1f).testTag("learned_total"))
            }
        }
        if (preferences.petKeepsakeIds.isNotEmpty()) {
            item {
                Text("小笺留给你的", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("这些小礼物记下了认真学习的日子。", color = MutedInk, style = MaterialTheme.typography.bodyMedium)
            }
            MotivationRules.keepsakes.filter { it.id in preferences.petKeepsakeIds }.forEach { keepsake ->
                item(key = keepsake.id) { KeepsakeCard(keepsake) }
            }
        }
        item {
            Text(
                "成长规则：每篇新完成的篇目会获得一片成长叶；重复练习仍然很有价值，但不会重复计数。",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedInk
            )
        }
    }
}

private fun stageTitle(level: Int) = when (level) {
    1 -> "初芽 · 相识"
    2 -> "展叶 · 共读"
    3 -> "新枝 · 相伴"
    4 -> "花庭 · 守望"
    else -> "满园 · 远行"
}

private fun stageNarrative(level: Int) = when (level) {
    1 -> "一颗种子、一本书，故事刚刚开始。"
    2 -> "藤蔓绕过书页，读书的小角落有了新绿。"
    3 -> "书灯亮起，枝叶替你们收藏每一次坚持。"
    4 -> "花架与小亭落成，花园正在安静地守望。"
    else -> "拱门、繁花和星光，已为下一段旅程点亮。"
}

@Composable
private fun PetStageArtwork(
    level: Int,
    scale: Float,
    rotation: Float,
    sway: Float,
    reacting: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().height(276.dp).padding(top = 8.dp)
            .clickable(onClick = onClick)
            .testTag("pet_character")
            .semantics { contentDescription = "${stageTitle(level)}的小笺插画。轻触小笺可以摸摸她的头并听她回应。" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) { drawGardenScene(level, sway) }
        Image(
            painter = painterResource(R.drawable.xiaojian_anime_companion),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().padding(vertical = 4.dp, horizontal = 12.dp)
                .scale(scale)
                .graphicsLayer { rotationZ = rotation }
        )
        AnimatedVisibility(visible = reacting, modifier = Modifier.align(Alignment.TopEnd).padding(top = 20.dp, end = 26.dp)) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = "小笺开心地回应了你的触碰", tint = Color(0xFFE5AF45), modifier = Modifier.size(28.dp))
        }
    }
}

private fun DrawScope.drawGardenScene(level: Int, sway: Float) {
    val sky = Color(0xFFF6F1DD)
    val grass = Color(0xFFC9DEB9)
    val deepLeaf = Color(0xFF527F59)
    val leaf = Color(0xFF84AD6F)
    val blossom = Color(0xFFE6A9B7)
    val paper = Color(0xFFFFFAEE)
    val wood = Color(0xFF9A7251)
    val gold = Color(0xFFE4BB58)
    drawRoundRect(sky, cornerRadius = androidx.compose.ui.geometry.CornerRadius(36f, 36f))
    drawOval(grass, topLeft = Offset(0f, size.height * .67f), size = androidx.compose.ui.geometry.Size(size.width, size.height * .35f))
    when (level) {
        1 -> {
            drawPebblePath(wood)
            drawSprout(size.width * .18f, size.height * .76f, leaf, deepLeaf, sway)
            drawPaperSeedling(paper, deepLeaf)
        }
        2 -> {
            drawPebblePath(wood)
            drawReadingBench(wood, paper)
            drawVine(size.width * .13f, size.height * .25f, leaf, deepLeaf, sway)
            drawVine(size.width * .87f, size.height * .31f, leaf, deepLeaf, -sway)
        }
        3 -> {
            drawReadingBench(wood, paper)
            drawBookLamp(size.width * .18f, size.height * .69f, wood, gold)
            drawShrub(size.width * .84f, size.height * .78f, leaf, deepLeaf, 1.1f)
            drawShrub(size.width * .16f, size.height * .83f, leaf, deepLeaf, .8f)
            drawVine(size.width * .89f, size.height * .21f, leaf, deepLeaf, sway)
        }
        4 -> {
            drawPavilion(wood, paper)
            drawFlowerBed(size.width * .16f, size.height * .79f, leaf, deepLeaf, blossom)
            drawFlowerBed(size.width * .85f, size.height * .80f, leaf, deepLeaf, blossom)
            drawHangingLantern(size.width * .18f, size.height * .26f, wood, gold, sway)
            drawHangingLantern(size.width * .82f, size.height * .24f, wood, gold, -sway)
        }
        else -> {
            drawGardenArch(wood, leaf, blossom)
            drawFlowerBed(size.width * .13f, size.height * .80f, leaf, deepLeaf, blossom)
            drawFlowerBed(size.width * .88f, size.height * .80f, leaf, deepLeaf, blossom)
            drawBookLamp(size.width * .19f, size.height * .67f, wood, gold)
            drawBookLamp(size.width * .81f, size.height * .67f, wood, gold)
            drawStar(size.width * .18f, size.height * .17f, gold)
            drawStar(size.width * .82f, size.height * .20f, gold)
            drawStar(size.width * .72f, size.height * .11f, gold)
        }
    }
}

private fun DrawScope.drawPebblePath(color: Color) {
    repeat(5) { index ->
        val y = size.height * (.73f + index * .05f)
        val x = size.width * (.43f + (index % 2) * .04f)
        drawOval(color.copy(alpha = .38f), topLeft = Offset(x, y), size = androidx.compose.ui.geometry.Size(28f, 12f))
    }
}

private fun DrawScope.drawSprout(x: Float, y: Float, leaf: Color, deepLeaf: Color, sway: Float) {
    drawLine(deepLeaf, Offset(x, y), Offset(x + sway * 2f, y - 64f), strokeWidth = 7f)
    drawOval(leaf, topLeft = Offset(x - 26f + sway, y - 67f), size = androidx.compose.ui.geometry.Size(34f, 20f))
    drawOval(leaf, topLeft = Offset(x + 2f + sway, y - 48f), size = androidx.compose.ui.geometry.Size(34f, 20f))
}

private fun DrawScope.drawPaperSeedling(paper: Color, ink: Color) {
    val rect = Rect(size.width * .68f, size.height * .71f, size.width * .84f, size.height * .84f)
    drawRoundRect(paper, topLeft = rect.topLeft, size = rect.size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f))
    drawLine(ink.copy(alpha = .45f), Offset(rect.left + 14f, rect.top + 38f), Offset(rect.right - 12f, rect.top + 38f), 3f)
    drawLine(ink.copy(alpha = .45f), Offset(rect.left + 14f, rect.top + 62f), Offset(rect.right - 25f, rect.top + 62f), 3f)
}

private fun DrawScope.drawReadingBench(wood: Color, paper: Color) {
    val y = size.height * .74f
    drawRoundRect(wood, topLeft = Offset(size.width * .18f, y), size = androidx.compose.ui.geometry.Size(size.width * .64f, 16f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))
    drawLine(wood, Offset(size.width * .28f, y + 13f), Offset(size.width * .25f, y + 42f), 9f)
    drawLine(wood, Offset(size.width * .72f, y + 13f), Offset(size.width * .75f, y + 42f), 9f)
    drawRoundRect(paper, topLeft = Offset(size.width * .43f, y - 30f), size = androidx.compose.ui.geometry.Size(size.width * .17f, 34f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f))
    drawLine(wood.copy(alpha = .5f), Offset(size.width * .515f, y - 27f), Offset(size.width * .515f, y), 2f)
}

private fun DrawScope.drawVine(x: Float, y: Float, leaf: Color, deepLeaf: Color, sway: Float) {
    val path = Path().apply {
        moveTo(x, y)
        cubicTo(x + 24f, y + 36f, x - 10f + sway * 3f, y + 88f, x + 38f, y + 126f)
    }
    drawPath(path, deepLeaf, style = Stroke(width = 6f))
    listOf(Offset(x + 12f, y + 35f), Offset(x + 2f, y + 78f), Offset(x + 37f, y + 108f)).forEachIndexed { index, point ->
        drawOval(leaf, topLeft = Offset(point.x - 14f + sway * index, point.y - 8f), size = androidx.compose.ui.geometry.Size(25f, 15f))
    }
}

private fun DrawScope.drawBookLamp(x: Float, y: Float, wood: Color, gold: Color) {
    drawLine(wood, Offset(x, y), Offset(x, y - 58f), 7f)
    drawLine(wood, Offset(x, y - 57f), Offset(x + 23f, y - 71f), 6f)
    drawCircle(gold.copy(alpha = .35f), radius = 26f, center = Offset(x + 24f, y - 71f))
    drawCircle(gold, radius = 10f, center = Offset(x + 24f, y - 71f))
}

private fun DrawScope.drawShrub(x: Float, y: Float, leaf: Color, deepLeaf: Color, multiplier: Float) {
    drawCircle(deepLeaf, radius = 30f * multiplier, center = Offset(x, y))
    drawCircle(leaf, radius = 25f * multiplier, center = Offset(x - 20f, y - 12f))
    drawCircle(leaf, radius = 24f * multiplier, center = Offset(x + 20f, y - 16f))
}

private fun DrawScope.drawPavilion(wood: Color, paper: Color) {
    val left = size.width * .18f
    val right = size.width * .82f
    val roofY = size.height * .31f
    drawLine(wood, Offset(left + 44f, roofY), Offset(left + 44f, size.height * .73f), 11f)
    drawLine(wood, Offset(right - 44f, roofY), Offset(right - 44f, size.height * .73f), 11f)
    val roof = Path().apply { moveTo(left, roofY); lineTo(size.width * .5f, roofY - 62f); lineTo(right, roofY); close() }
    drawPath(roof, wood)
    drawRoundRect(paper.copy(alpha = .82f), topLeft = Offset(left + 26f, roofY + 9f), size = androidx.compose.ui.geometry.Size(right - left - 52f, 22f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))
}

private fun DrawScope.drawFlowerBed(x: Float, y: Float, leaf: Color, deepLeaf: Color, blossom: Color) {
    drawShrub(x, y, leaf, deepLeaf, .9f)
    listOf(Offset(x - 20f, y - 30f), Offset(x + 5f, y - 43f), Offset(x + 27f, y - 26f)).forEach { center ->
        drawCircle(blossom, radius = 10f, center = center)
        drawCircle(Color(0xFFF3D26B), radius = 3f, center = center)
    }
}

private fun DrawScope.drawHangingLantern(x: Float, y: Float, wood: Color, gold: Color, sway: Float) {
    drawLine(wood, Offset(x, y - 35f), Offset(x + sway * 4f, y), 4f)
    drawRoundRect(gold, topLeft = Offset(x - 10f + sway * 4f, y), size = androidx.compose.ui.geometry.Size(20f, 27f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f))
}

private fun DrawScope.drawGardenArch(wood: Color, leaf: Color, blossom: Color) {
    val left = size.width * .12f
    val right = size.width * .88f
    val bottom = size.height * .78f
    drawLine(wood, Offset(left, bottom), Offset(left, size.height * .39f), 12f)
    drawLine(wood, Offset(right, bottom), Offset(right, size.height * .39f), 12f)
    val arch = Path().apply { moveTo(left, size.height * .42f); cubicTo(left, size.height * .03f, right, size.height * .03f, right, size.height * .42f) }
    drawPath(arch, wood, style = Stroke(width = 12f))
    listOf(.20f, .32f, .68f, .80f).forEachIndexed { index, fraction ->
        val x = size.width * fraction
        val y = size.height * (if (index % 2 == 0) .25f else .18f)
        drawCircle(leaf, radius = 17f, center = Offset(x, y))
        drawCircle(blossom, radius = 7f, center = Offset(x + 10f, y - 5f))
    }
}

private fun DrawScope.drawStar(x: Float, y: Float, color: Color) {
    val path = Path().apply {
        moveTo(x, y - 13f); lineTo(x + 4f, y - 4f); lineTo(x + 13f, y); lineTo(x + 4f, y + 4f)
        lineTo(x, y + 13f); lineTo(x - 4f, y + 4f); lineTo(x - 13f, y); lineTo(x - 4f, y - 4f); close()
    }
    drawPath(path, color)
}

@Composable
private fun KeepsakeCard(keepsake: PetKeepsake) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(keepsake.title, fontWeight = FontWeight.SemiBold, color = DeepGreen)
            Text(keepsake.description, color = MutedInk, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 3.dp))
        }
    }
}

@Composable
private fun GardenMetric(label: String, value: String, modifier: Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = MutedInk, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
