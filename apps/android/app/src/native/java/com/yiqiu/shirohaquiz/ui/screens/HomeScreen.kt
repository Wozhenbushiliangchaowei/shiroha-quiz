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
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.R
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.IllustrationHeroCard
import com.yiqiu.shirohaquiz.ui.components.MetricGlassCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.ShortcutGlassCard
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing

@Composable
fun HomeScreen(
    onGoImport: () -> Unit,
    onGoPractice: () -> Unit,
    onGoExam: () -> Unit,
    onOpenBankDetail: (String) -> Unit,
    onOpenWrongBook: () -> Unit,
    onOpenRecords: () -> Unit
) {
    val activeBank = QuizRepository.activeBank()
    val bankCount = QuizRepository.banks.size
    val questionCount = activeBank?.questions?.size ?: 0
    val wrongCount = QuizRepository.wrongBook.size
    val recordCount = QuizRepository.studyRecords.size

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(ShirohaSpacing.Xl),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Shiroha Quiz",
            title = "原生题库首页",
            subtitle = "这里是原生 Android 主流程。导入后的题库会直接进入原生状态，并接入练习、考试、错题本和学习记录。"
        )

        IllustrationHeroCard(
            title = "欢迎回来",
            subtitle = "这张小头像只承担欢迎和识别感，不去抢首页主信息的空间。",
            imageRes = R.drawable.illus_home_welcome,
            imageSize = 84.dp
        ) {
            Text(
                text = if (questionCount > 0) "今天可以继续把 ${activeBank?.name} 往前推一轮。" else "先导入一份题库，我们就能把原生链真正跑起来。",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        GlassCard {
            Text(
                text = "当前题库",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = activeBank?.name ?: "尚未导入题库",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (questionCount > 0) {
                    "当前题库共有 $questionCount 题，已经可以直接进入原生练习和考试。"
                } else {
                    "当前还没有真实导入题目。先去导入页完成一份标准题库的导入。"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionPillButton(Icons.Rounded.CloudUpload, "去导入", primary = true, onClick = onGoImport)
                ActionPillButton(Icons.Rounded.PlayArrow, "进入练习", primary = false, onClick = onGoPractice)
                ActionPillButton(Icons.Rounded.Timer, "开始考试", primary = false, onClick = onGoExam)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricGlassCard(
                label = "题库数量",
                value = bankCount.toString(),
                desc = "原生侧当前挂载的题库",
                modifier = Modifier.weight(1f)
            )
            MetricGlassCard(
                label = "当前题量",
                value = questionCount.toString(),
                desc = "活动题库中的题目数",
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricGlassCard(
                label = "错题数",
                value = wrongCount.toString(),
                desc = "练习和考试中累计的错题",
                modifier = Modifier.weight(1f)
            )
            MetricGlassCard(
                label = "记录数",
                value = recordCount.toString(),
                desc = "原生学习记录条目",
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShortcutGlassCard(
                title = "标准文本导入",
                icon = Icons.Rounded.AutoStories,
                desc = "优先覆盖最常见的题库格式",
                modifier = Modifier.weight(1f)
            )
            ShortcutGlassCard(
                title = "原生考试模式",
                icon = Icons.Rounded.Schedule,
                desc = "题量、计时、交卷和结果页已经接通",
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShortcutGlassCard(
                title = "错题本",
                icon = Icons.Rounded.ReportProblem,
                desc = "收拢练习和考试里的错题",
                modifier = Modifier.weight(1f)
            )
            ShortcutGlassCard(
                title = "学习记录",
                icon = Icons.Rounded.History,
                desc = "查看最近的练习和考试结果",
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionPillButton(Icons.Rounded.ReportProblem, "打开错题本", primary = false, onClick = onOpenWrongBook)
            ActionPillButton(Icons.Rounded.History, "查看记录", primary = false, onClick = onOpenRecords)
        }

        GlassCard {
            Text(
                text = "题库列表",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            QuizRepository.banks.forEach { bank ->
                val isActive = bank.id == activeBank?.id
                Text(
                    text = bank.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = buildString {
                        append("${bank.questions.size} 题")
                        if (isActive) append(" · 当前活动题库")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionPillButton(
                        icon = Icons.Rounded.PlayArrow,
                        text = "查看详情",
                        primary = isActive,
                        onClick = { onOpenBankDetail(bank.id) }
                    )
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}
