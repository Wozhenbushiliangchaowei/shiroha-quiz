package com.yiqiu.shirohaquiz.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.R
import com.yiqiu.shirohaquiz.importer.model.ImportResult
import com.yiqiu.shirohaquiz.importer.model.Question
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import com.yiqiu.shirohaquiz.importer.model.WarningLevel
import com.yiqiu.shirohaquiz.importer.parser.QuizImportParser
import com.yiqiu.shirohaquiz.importer.parser.TextImportDecoder
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.IllustrationHeroCard
import com.yiqiu.shirohaquiz.ui.components.LoadingIllustration
import com.yiqiu.shirohaquiz.ui.components.NoticeCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.StatusChip
import com.yiqiu.shirohaquiz.ui.components.UploadPanel
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaRadius
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImportScreen(
    onImportSaved: () -> Unit
) {
    val context = LocalContext.current
    var rawText by rememberSaveable { mutableStateOf(sampleImportText()) }
    var answerText by rememberSaveable { mutableStateOf(sampleAnswerText()) }
    var selectedFileName by rememberSaveable { mutableStateOf("未选择文件") }
    var selectedAnswerFileName by rememberSaveable { mutableStateOf("未选择答案文件") }
    var importResult by remember { mutableStateOf<ImportResult?>(null) }
    var statusText by rememberSaveable {
        mutableStateOf("当前先接入原生标准题库导入。推荐优先导入 txt / json / csv / docx 这类结构化文本文件。")
    }
    var isStatusWarn by rememberSaveable { mutableStateOf(false) }
    var useDualImport by rememberSaveable { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        selectedFileName = queryFileName(context, uri)
        val text = readImportedText(context, uri, selectedFileName)
        if (text.isNullOrBlank()) {
            importResult = null
            statusText = "当前原生导入第一版还不能稳定读取这个文件。建议优先使用 txt / csv / json / docx。"
            isStatusWarn = true
        } else {
            rawText = text
            importResult = null
            statusText = "已读取文件：$selectedFileName，可以直接开始原生解析。"
            isStatusWarn = false
        }
    }

    val answerFilePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        selectedAnswerFileName = queryFileName(context, uri)
        val text = readImportedText(context, uri, selectedAnswerFileName)
        if (text.isNullOrBlank()) {
            statusText = "答案文件暂时还不能稳定读取，请优先使用 txt 或可复制文本的文档。"
            isStatusWarn = true
        } else {
            answerText = text
            importResult = null
            statusText = "已读取答案文件：$selectedAnswerFileName。"
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
            subtitle = "这里走的是真正的 Kotlin 原生导入链。当前优先覆盖标准文本和双文件导入，再逐步补齐更多格式。"
        )

        IllustrationHeroCard(
            title = "先导入，再核对，最后创建题库",
            subtitle = "导入页最适合放引导插画。它负责帮用户理解流程，而不是挤占主操作区。",
            imageRes = R.drawable.illus_import_hint,
            imageSize = 124.dp
        )

        GlassCard {
            Text(
                text = "当前进度",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusChip("标准文本导入", selected = !useDualImport)
                StatusChip("原生结果预览", selected = true)
                StatusChip("文件选择", selected = true)
                StatusChip("答案区解析", selected = true)
                StatusChip("双文件导入", selected = useDualImport)
            }
            Spacer(Modifier.height(14.dp))
            NoticeCard(statusText, warning = isStatusWarn)
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
                desc = "当前原生第一版建议导入 txt / csv / json / docx 等文本型文件，后面再继续补 pdf。",
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
                        useDualImport = false
                        selectedFileName = "示例题库"
                        rawText = sampleImportText()
                        importResult = null
                        statusText = "已填入示例标准题库，可以直接测试原生解析。"
                        isStatusWarn = false
                    }
                )
            }
            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ImportModeChip(
                    icon = Icons.Rounded.Description,
                    text = "标准导入",
                    selected = !useDualImport,
                    onClick = {
                        useDualImport = false
                        importResult = null
                        statusText = "已切换到原生标准导入模式。"
                    }
                )
                ImportModeChip(
                    icon = Icons.Rounded.AutoAwesome,
                    text = "双文件导入",
                    selected = useDualImport,
                    onClick = {
                        useDualImport = true
                        importResult = null
                        statusText = "已切换到原生双文件导入模式。"
                    }
                )
            }
        }

        GlassCard {
            Text(
                text = if (useDualImport) "题目文本" else "原始文本",
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

            if (useDualImport) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "答案文本",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = answerText,
                    onValueChange = {
                        answerText = it
                        importResult = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    minLines = 8,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    placeholder = { Text("粘贴答案文本，或通过下方按钮选择答案文件。") }
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "当前答案文件：$selectedAnswerFileName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                ActionPillButton(
                    icon = Icons.Rounded.FileOpen,
                    text = "选择答案文件",
                    primary = false,
                    onClick = { answerFilePicker.launch(arrayOf("*/*")) }
                )
            }

            Spacer(Modifier.height(14.dp))
            ActionPillButton(
                icon = Icons.Rounded.PlayArrow,
                text = if (useDualImport) "开始双文件解析" else "开始原生解析",
                primary = true,
                onClick = {
                    if (rawText.isBlank() || (useDualImport && answerText.isBlank())) {
                        statusText = if (useDualImport) {
                            "请同时提供题目文本和答案文本，再开始双文件解析。"
                        } else {
                            "请先提供题库文本，再开始原生解析。"
                        }
                        isStatusWarn = true
                    } else {
                        importResult = if (useDualImport) {
                            QuizImportParser.parseDualText(rawText, answerText)
                        } else {
                            QuizImportParser.parseStandardText(rawText)
                        }
                        val result = importResult
                        val hardCount = result?.warnings?.count { it.level == WarningLevel.ERROR } ?: 0
                        val softCount = result?.warnings?.count { it.level == WarningLevel.WARNING } ?: 0
                        statusText = "已完成${if (useDualImport) "双文件" else "原生"}解析：${result?.questions?.size ?: 0} 题，硬错误 $hardCount 条，可确认提示 $softCount 条。"
                        isStatusWarn = hardCount > 0
                    }
                }
            )
        }

        importResult?.let { result ->
            NativeImportSummary(result)

            GlassCard {
                Text(
                    text = "写入原生题库",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "确认无误后，把当前解析结果写入原生题库状态。首页、练习、考试、错题本和记录页都会直接使用这批数据。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                ActionPillButton(
                    icon = Icons.Rounded.PlayArrow,
                    text = "保存为当前题库",
                    primary = true,
                    onClick = {
                        val bankName = selectedFileName.substringBeforeLast('.').ifBlank { "导入题库" }
                        QuizRepository.importBank(context, bankName, result.questions)
                        statusText = "已写入原生题库：$bankName，共 ${result.questions.size} 题。现在可以切到首页、练习或考试查看。"
                        isStatusWarn = false
                        onImportSaved()
                    }
                )
            }

            NativeImportPreview(result.questions)
        }

        if (importResult == null && rawText.isNotBlank()) {
            LoadingIllustration("准备好以后，点击“开始原生解析”，这里会切成结构化预览。")
        }
    }
}

@Composable
private fun ImportModeChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(ShirohaRadius.Pill),
        color = if (selected) ShirohaColors.BrandPrimarySoft else Color.White.copy(alpha = 0.84f),
        border = BorderStroke(
            1.dp,
            if (selected) ShirohaColors.LineSelected else ShirohaColors.LineStrong
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NativeImportSummary(result: ImportResult) {
    val hardCount = result.warnings.count { it.level == WarningLevel.ERROR }
    val softCount = result.warnings.count { it.level == WarningLevel.WARNING }

    GlassCard {
        Text(
            text = "解析结果",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatusChip("策略：${result.strategyName}", selected = true)
            StatusChip("识别题数：${result.questions.size}", selected = true)
            StatusChip("硬错误：$hardCount", selected = hardCount == 0)
            StatusChip("提示：$softCount", selected = softCount == 0)
        }
        if (result.warnings.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            result.warnings.take(6).forEach { warning ->
                NoticeCard("第 ${warning.questionNumber ?: "-"} 题：${warning.message}")
                Spacer(Modifier.height(8.dp))
            }
        }
        if (result.diagnostics.notes.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "候选策略诊断",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            result.diagnostics.notes.take(4).forEach { note ->
                Text(
                    text = "• $note",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
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

private fun readImportedText(context: Context, uri: Uri, fileName: String): String? {
    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
    return TextImportDecoder.decode(bytes, fileName)
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
答案：对
解析：这是一道基础判断题，答案为正确。
""".trimIndent()

private fun sampleAnswerText(): String = """
1. A
2. AB
3. 对
""".trimIndent()
