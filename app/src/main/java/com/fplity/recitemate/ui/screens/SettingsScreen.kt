package com.fplity.recitemate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fplity.recitemate.data.local.FontSize
import com.fplity.recitemate.data.local.ReadingTheme
import com.fplity.recitemate.ui.theme.DeepGreen
import com.fplity.recitemate.ui.theme.MutedInk
import com.fplity.recitemate.ui.theme.PaleGreen

@Composable
fun SettingsScreen(
    fontSize: FontSize,
    readingTheme: ReadingTheme,
    onFontSizeChange: (FontSize) -> Unit,
    onReadingThemeChange: (ReadingTheme) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { Text("设置", style = MaterialTheme.typography.headlineMedium, color = DeepGreen, fontWeight = FontWeight.Bold) }
        item {
            SettingsCard("字体大小", "阅读文章时的正文大小") {
                FontSize.entries.forEach { option ->
                    SettingOption(option.label, option == fontSize) { onFontSizeChange(option) }
                }
            }
        }
        item {
            SettingsCard("阅读模式", "选择界面显示方式") {
                ReadingTheme.entries.forEach { option ->
                    SettingOption(option.label, option == readingTheme) { onReadingThemeChange(option) }
                }
            }
        }
        item {
            SettingsCard("关于青简", "本地高中古诗文阅读工具") {
                Text("所有文章内容保存在本机，不需要登录或联网。", style = MaterialTheme.typography.bodyMedium, color = MutedInk, modifier = Modifier.padding(top = 8.dp))
                Text("版本 1.0", style = MaterialTheme.typography.labelMedium, color = MutedInk, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PaleGreen),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MutedInk, modifier = Modifier.padding(top = 4.dp))
            content()
        }
    }
}

@Composable
private fun SettingOption(label: String, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).then(androidx.compose.ui.Modifier),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, modifier = Modifier.padding(start = 6.dp), style = MaterialTheme.typography.bodyLarge)
    }
}
