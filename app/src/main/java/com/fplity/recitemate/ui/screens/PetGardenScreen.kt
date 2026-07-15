package com.fplity.recitemate.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fplity.recitemate.R
import com.fplity.recitemate.data.local.MotivationRules
import com.fplity.recitemate.ui.theme.DeepGreen
import com.fplity.recitemate.ui.theme.MutedInk
import com.fplity.recitemate.ui.theme.PaleGreen

@Composable
fun PetGardenScreen(leaves: Int, streak: Int, learnedCount: Int) {
    val level = MotivationRules.petLevelForLeaves(leaves)
    val nextThreshold = MotivationRules.nextLevelThreshold(leaves)
    val visual = when (level) {
        1 -> "小笺 · 初芽"
        2 -> "小笺 · 展叶"
        3 -> "小笺 · 新枝"
        4 -> "小笺 · 花园"
        else -> "小笺 · 满园"
    }
    val levelStart = listOf(0, 5, 15, 30, 50)[level - 1]
    val progress = if (nextThreshold == null) 1f else (leaves - levelStart).toFloat() / (nextThreshold - levelStart)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("小笺", style = MaterialTheme.typography.headlineMedium, color = DeepGreen, fontWeight = FontWeight.Bold)
            Text("每次如实完成学习，都会让这座小花园长出一片叶子。", color = MutedInk, modifier = Modifier.padding(top = 5.dp))
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PaleGreen),
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.fillMaxWidth().testTag("pet_status")
                    .semantics { contentDescription = "小笺等级${level}，当前${leaves}片叶子" }
            ) {
                Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(visual, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Image(
                        painter = painterResource(R.drawable.xiaojian_anime_companion),
                        contentDescription = "小笺成长伙伴插画",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth().height(220.dp).padding(top = 12.dp)
                    )
                    Text("等级 $level", style = MaterialTheme.typography.titleMedium, color = DeepGreen, modifier = Modifier.padding(top = 8.dp).testTag("pet_level"))
                    Text("${leaves} 片叶子", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 4.dp).testTag("leaf_total"))
                    LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
                    Text(
                        nextThreshold?.let { "再收集 ${it - leaves} 片叶子，解锁下一阶段" } ?: "你已解锁小笺的全部成长阶段！",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedInk,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GardenMetric("连续学习", "$streak 天", Modifier.weight(1f).testTag("streak_total"))
                GardenMetric("已学篇目", "$learnedCount 篇", Modifier.weight(1f).testTag("learned_total"))
            }
        }
        item {
            Text("成长规则：0、5、15、30、50 片叶子分别到达第 1 至第 5 级。学习可以慢一点，但每一次如实记录都算数。", style = MaterialTheme.typography.bodyMedium, color = MutedInk)
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
