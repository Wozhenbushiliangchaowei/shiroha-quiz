package com.yiqiu.shirohaquiz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.ReportProblem
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
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing

@Composable
fun MeScreen(
    onOpenWrongBook: () -> Unit,
    onOpenRecords: () -> Unit
) {
    Column(
        modifier = Modifier.padding(ShirohaSpacing.Xl),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Me",
            title = "设置与资料",
            subtitle = "这里先承接原生错题本、学习记录和后续设置入口。导出、主题和同步会在这条线继续补齐。"
        )
        IllustrationHeroCard(
            title = "资料页先保持轻一点",
            subtitle = "这张小图只做顶部识别，不把设置页做成一排排带大图的卡片。",
            imageRes = R.drawable.illus_home_welcome,
            imageSize = 72.dp
        )
        GlassCard {
            Text(
                text = "当前原生状态",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "题库 ${QuizRepository.banks.size} 份 · 错题 ${QuizRepository.wrongBook.size} 条 · 记录 ${QuizRepository.studyRecords.size} 条",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionPillButton(
                    icon = Icons.Rounded.ReportProblem,
                    text = "打开错题本",
                    primary = false,
                    onClick = onOpenWrongBook
                )
                ActionPillButton(
                    icon = Icons.Rounded.History,
                    text = "查看记录",
                    primary = false,
                    onClick = onOpenRecords
                )
            }
        }
    }
}
