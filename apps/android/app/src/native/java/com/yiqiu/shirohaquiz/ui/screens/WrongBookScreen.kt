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
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.PlayArrow
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
fun WrongBookScreen(
    onBack: () -> Unit,
    onGoPractice: () -> Unit
) {
    val wrongBook = QuizRepository.wrongBook

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(ShirohaSpacing.Xl),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Wrong Book",
            title = "原生错题本",
            subtitle = "这里收拢练习和考试里答错的题。进入练习后会自动切到对应题目，方便我们就地复习。"
        )

        if (wrongBook.isEmpty()) {
            EmptyStateIllustration(
                title = "错题本还是空的",
                message = "继续练习一轮，这里就会慢慢充实起来。空状态图比硬塞一张常驻大图更合适。",
                imageRes = R.drawable.illus_wrongbook_hint,
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
            title = "错题会自动沉淀在这里",
            subtitle = "这张图只放在错题入口附近，提醒用户这里是复习和回顾的地方。",
            imageRes = R.drawable.illus_wrongbook_hint,
            imageSize = 112.dp
        )

        GlassCard {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusChip("错题 ${wrongBook.size} 条", selected = true)
                ActionPillButton(
                    icon = Icons.Rounded.DeleteOutline,
                    text = "清空错题本",
                    primary = false,
                    onClick = { QuizRepository.clearWrongBook() }
                )
            }
        }

        wrongBook.forEach { entry ->
            GlassCard {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(entry.source, selected = true)
                    StatusChip(entry.bankName)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "${entry.question.number}. ${entry.question.question}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "你的答案：${entry.lastAnswer.joinToString(" / ").ifBlank { "未作答" }}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "正确答案：${entry.question.answer.joinToString(" / ").ifBlank { "未识别答案" }}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (entry.question.analysis.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "解析：${entry.question.analysis}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "记录时间：${formatTimestamp(entry.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionPillButton(
                        icon = Icons.Rounded.PlayArrow,
                        text = "去练这题",
                        primary = true,
                        onClick = {
                            QuizRepository.openWrongQuestion(entry)
                            onGoPractice()
                        }
                    )
                    ActionPillButton(
                        icon = Icons.Rounded.DeleteOutline,
                        text = "移出错题本",
                        primary = false,
                        onClick = { QuizRepository.removeWrongQuestion(entry) }
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}
