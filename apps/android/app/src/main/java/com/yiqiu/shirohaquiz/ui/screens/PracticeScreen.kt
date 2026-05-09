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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.TextSnippet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.NoticeCard
import com.yiqiu.shirohaquiz.ui.components.QuizOptionCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.StatusChip
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing

@Composable
fun PracticeScreen() {
    val bank = QuizRepository.activeBank()
    val question = QuizRepository.currentPracticeQuestion()
    var submitted by remember(question?.id) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(ShirohaSpacing.Xl),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Practice",
            title = "原生练习页",
            subtitle = if (bank == null || bank.questions.isEmpty()) {
                "当前还没有可练习的题目。先去导入页导入一份标准题库。"
            } else {
                "当前题库：${bank.name}，共 ${bank.questions.size} 题。这里已经开始读取真实导入结果。"
            }
        )

        if (question == null) {
            GlassCard {
                NoticeCard("还没有可练习题目。请先在原生导入页完成一次标准题库导入。")
            }
            return
        }

        GlassCard {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip("第 ${QuizRepository.practiceIndex + 1} 题", selected = true)
                StatusChip(typeLabel(question.type))
                StatusChip("原生题库")
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = question.question,
                style = MaterialTheme.typography.headlineSmall,
                lineHeight = 34.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(18.dp))

            when (question.type) {
                QuestionType.SINGLE,
                QuestionType.MULTIPLE,
                QuestionType.JUDGE -> {
                    question.options.forEach { option ->
                        QuizOptionCard(
                            label = option.key,
                            text = option.text,
                            selected = QuizRepository.selectedAnswer.contains(option.key)
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }

                QuestionType.BLANK,
                QuestionType.SHORT -> {
                    NoticeCard("这道题属于 ${typeLabel(question.type)}，当前练习页先展示参考答案和解析。后面再继续补原生主观题作答交互。")
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionPillButton(
                    Icons.Rounded.ArrowBack,
                    "上一题",
                    primary = false,
                    onClick = {
                        QuizRepository.previousQuestion()
                        submitted = false
                    }
                )
                ActionPillButton(
                    Icons.Rounded.ArrowForward,
                    "下一题",
                    primary = false,
                    onClick = {
                        QuizRepository.nextQuestion()
                        submitted = false
                    }
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionPillButton(
                    Icons.Rounded.CheckCircle,
                    "提交答案",
                    onClick = { submitted = true }
                )
                ActionPillButton(
                    Icons.Rounded.TextSnippet,
                    "查看解析",
                    primary = false,
                    onClick = { submitted = true }
                )
            }

            if (submitted) {
                Spacer(Modifier.height(16.dp))
                val answerText = question.answer.joinToString(" / ").ifBlank { "未识别答案" }
                NoticeCard("正确答案：$answerText")
                if (question.analysis.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "解析：${question.analysis}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun typeLabel(type: QuestionType): String = when (type) {
    QuestionType.SINGLE -> "单选题"
    QuestionType.MULTIPLE -> "多选题"
    QuestionType.JUDGE -> "判断题"
    QuestionType.BLANK -> "填空题"
    QuestionType.SHORT -> "简答题"
}
