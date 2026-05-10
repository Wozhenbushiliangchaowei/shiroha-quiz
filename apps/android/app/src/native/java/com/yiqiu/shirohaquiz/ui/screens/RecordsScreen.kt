package com.yiqiu.shirohaquiz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.R
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.EmptyStateIllustration
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.IllustrationHeroCard
import com.yiqiu.shirohaquiz.ui.components.NoticeCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.StatusChip
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordsScreen(
    onBack: () -> Unit
) {
    val records = QuizRepository.studyRecords

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(ShirohaSpacing.Xl),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Records",
            title = "原生学习记录",
            subtitle = "这里保留最近的练习提交和考试结果，方便我们回看进度，也为后面的统计页打基础。"
        )

        if (records.isEmpty()) {
            EmptyStateIllustration(
                title = "这里还没有学习记录",
                message = "只要先做一题练习，或者完成一场考试，记录页就会开始有内容。",
                imageRes = R.drawable.illus_rest_state,
                action = {
                    Spacer(Modifier.height(12.dp))
                }
            )
            GlassCard {
                ActionPillButton(
                    icon = Icons.AutoMirrored.Rounded.Undo,
                    text = "返回首页",
                    primary = false,
                    onClick = onBack
                )
            }
            return
        }

        IllustrationHeroCard(
            title = "学习记录会在这里慢慢积累",
            subtitle = "这张图适合做轻量回顾感，不会压住真正的记录列表。",
            imageRes = R.drawable.illus_rest_state,
            imageSize = 96.dp
        )

        records.forEach { record ->
            GlassCard {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(record.source, selected = true)
                    StatusChip(record.bankName)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = record.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "正确 ${record.correct} / ${record.total}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (record.durationSeconds != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "用时 ${formatDuration(record.durationSeconds)}${if (record.autoSubmitted) " · 自动交卷" else ""}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "记录时间：${formatRecordTime(record.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun formatRecordTime(timestamp: Long): String {
    return SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}
