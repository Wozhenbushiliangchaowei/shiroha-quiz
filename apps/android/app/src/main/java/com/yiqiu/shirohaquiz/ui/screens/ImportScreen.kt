package com.yiqiu.shirohaquiz.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.importer.model.ImportResult
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import com.yiqiu.shirohaquiz.importer.parser.QuizImportParser
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.NoticeCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.StatusChip
import com.yiqiu.shirohaquiz.ui.components.UploadPanel
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import java.nio.charset.Charset

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImportScreen() {
    val context = LocalContext.current
    var rawText by rememberSaveable { mutableStateOf(sampleImportText()) }
    var selectedFileName by rememberSaveable { mutableStateOf("未选择文件") }
    var importResult by remember { mutableStateOf<ImportResult?>(null) }
    var statusText by rememberSaveable { mutableStateOf("当前先接入原生标准题库导入。推荐先导入 txt / json / csv 这类文本文件。") }
    var isStatusWarn by rememberSaveable { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val fileName = queryFileName(context, uri)
        selectedFileName = fileName
        val text = readTextSafely(context, uri)
        if (text == null) {
            importResult = null
            statusText = "当前原生导入第一版只保证标准文本题库。这个文件还不能稳定读取，请先转成 txt 再导入。"
            isStatusWarn = true
        } else {
            rawText = text
            importResult = null
            statusText = "已读取文件：$fileName，可以直接开始原生解析。"
            isStatusWarn = false
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(ShirohaSpacing.Xl),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Import",
            title = "原生导入题库",
            subtitle = "这版开始接入真正的 Kotlin 原生导入链。当前先支持标准文本题库导入与原生预览。"
        )

        GlassCard {
            Text(
                text = "当前进度",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusChip("标准文本导入", selected = true)
                StatusChip("原生结果预览", selected = true)
                StatusChip("文件选择", selected = true)
                StatusChip("答案区解析", selected = false)
                StatusChip("双文件导入", selected = false)
            }
            Spacer(Modifier.height(14.dp))
            NoticeCard(statusText)
        }

        GlassCard {
            Text(
                text = "导入方式",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(14.dp))
            UploadPanel(
                title = "选择题库文件",
                desc = "当前原生第一版建议导入 txt / csv / json 等文本文件，后面再继续补 docx / pdf。",
                icon = Icons.Rounded.FileOpen
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "当前文件：$selectedFileName",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionPillButton(
                    icon = Icons.Rounded.FileOpen,
                    text = "选择文件",
                    primary = true,
                    onClick = { filePicker.launch(arrayOf("*/*")) }
                )
                ActionPillButton(
                    icon = Icons.Rounded.Refresh,
                    text = "填入示例",
                    primary = false,
                    onClick = {
                        selectedFileName = "示例题库"
                        rawText = sampleImportText()
                        importResult = null
                        statusText = "已填入示例标准题库，可以直接测试原生解析。"
                        isStatusWarn = false
                    }
                )
            }
        }

        GlassCard {
            Text(
                text = "原始文本",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = rawText,
                onValueChange = {
                    rawText = it
                    importResult = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                minLines = 10,
                textStyle = MaterialTheme.typography.bodyMedium,
                placeholder = { Text("把标准题库文本粘贴到这里，或通过上方选择文件导入。") }
            )
            Spacer(Modifier.height(14.dp))
            ActionPillButton(
                icon = Icons.Rounded.PlayArrow,
                text = "开始原生解析",
                primary = true,
                onClick = {
                    if (rawText.isBlank()) {
                        statusText = "请先提供题库文本，再开始原生解析。"
                        isStatusWarn = true
                    } else {
                        importResult = QuizImportParser.parseStandardText(rawText)
                        val result = importResult
                        val hardCount = result?.issues?.count { it.isHardError } ?: 0
                        val softCount = result?.issues?.count { !it.isHardError } ?: 0
                        statusText = "已完成原生解析：${result?.questions?.size ?: 0} 题，硬错误 $hardCount 条，可确认提示 $softCount 条。"
                        isStatusWarn = hardCount > 0
                    }
                }
            )
        }

        importResult?.let { result ->
            NativeImportSummary(result)
            NativeImportPreview(result.questions)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NativeImportSummary(result: ImportResult) {
    val hardCount = result.issues.count { it.isHardError }
    val softCount = result.issues.count { !it.isHardError }

    GlassCard {
        Text(
            text = "解析结果",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            StatusChip("策略：${result.strategyName}", selected = true)
            StatusChip("识别题数：${result.questions.size}", selected = true)
            StatusChip("硬错误：$hardCount", selected = hardCount == 0)
            StatusChip("提示：$softCount", selected = softCount == 0)
        }
        if (result.issues.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            result.issues.take(6).forEach { issue ->
                NoticeCard("第${issue.questionNumber}题：${issue.message}")
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun NativeImportPreview(questions: List<Question>) {
    GlassCard {
        Text(
            text = "原生预览",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        questions.take(12).forEach { question ->
            val answerText = question.answer.joinToString(" / ").ifBlank { "未识别答案" }
            val optionText = question.options.joinToString("  ") { "${it.key}. ${it.text}" }

            Text(
                text = "${question.number}. ${typeLabel(question.type)}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            SelectionContainer {
                Text(
                    text = question.question,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (optionText.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = optionText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "答案：$answerText",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (question.analysis.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "解析：${question.analysis}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(18.dp))
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

private fun queryFileName(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val index = it.getColumnIndex("_display_name")
        if (index >= 0 && it.moveToFirst()) {
            return it.getString(index) ?: "未命名文件"
        }
    }
    return uri.lastPathSegment ?: "未命名文件"
}

private fun readTextSafely(context: Context, uri: Uri): String? {
    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
    if (bytes.isEmpty()) return ""

    val utf8 = bytes.toString(Charsets.UTF_8)
    if ('�' !in utf8) return utf8

    return try {
        bytes.toString(Charset.forName("GB18030"))
    } catch (_: Exception) {
        utf8
    }
}

private fun sampleImportText(): String = """
1. 安全帽的主要作用是（A）
A. 保护头部
B. 装饰作用
C. 增加重量
D. 无实际作用
答案：A
解析：安全帽用于减轻坠落物和碰撞对头部造成的伤害。

2. 雨天驾驶时应注意哪些事项（AB）
A. 降低车速
B. 加大跟车距离
C. 急打方向
D. 紧急制动
答案：AB
解析：雨天路滑，应平稳控制车辆并留足安全距离。

3. 国家安全生产方针是“安全第一，预防为主”。（对）
A. 对
B. 错
答案：对
解析：这是常见的安全生产基础判断题。
""".trimIndent()
