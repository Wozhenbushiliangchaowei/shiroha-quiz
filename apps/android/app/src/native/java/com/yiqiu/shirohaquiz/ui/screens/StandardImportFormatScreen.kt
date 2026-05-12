package com.yiqiu.shirohaquiz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing

@Composable
fun StandardImportFormatScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Format",
            title = "标准导入格式",
            subtitle = "按这个格式整理题库，识别会更稳定。"
        )

        ActionPillButton(
            icon = Icons.AutoMirrored.Rounded.ArrowBack,
            text = "返回设置",
            primary = false,
            modifier = Modifier.height(42.dp),
            onClick = onBack
        )

        FormatSection(
            title = "一、单文件标准格式",
            body = "每道题建议包含题号、题干、选项、答案和解析。题号可以用 1.、1、【1】 等形式，但同一份题库尽量统一。",
            sample = """
1. 坚持“两个至上”价值导向是指（ ）至上和（ ）至上。
A. 人民；经济
B. 人民；生命
C. 经济；财产
D. 发展；安全
答案：B
解析：两个至上通常指人民至上、生命至上。
            """.trimIndent()
        )

        FormatSection(
            title = "二、多选题格式",
            body = "多选题答案可以写成 AB、A B、A、B 或 A/B。建议答案集中写在“答案：AB”这一行。",
            sample = """
2. 下列属于安全管理要求的是（ ）。
A. 作业前安全交底
B. 施工参数实时记录
C. 随意变更施工方案
D. 完工后资料归档
答案：ABD
解析：C 项不符合安全管理要求。
            """.trimIndent()
        )

        FormatSection(
            title = "三、判断题格式",
            body = "判断题可使用“正确/错误”“对/错”“√/×”。不建议把答案混在很长的题干中。",
            sample = """
3. 固井施工前应进行安全交底。（ ）
答案：正确
解析：施工前安全交底是基础要求。
            """.trimIndent()
        )

        FormatSection(
            title = "四、双文件导入格式",
            body = "题目文件只放题干和选项，答案文件按题号列出答案。题号需要和题目文件对应。",
            sample = """
题目文件：
1. 题干……
A. 选项一
B. 选项二

答案文件：
1. B
2. AD
3. 正确
            """.trimIndent()
        )

        FormatSection(
            title = "五、减少识别错误的建议",
            body = "尽量避免把多个题目挤在一行；选项前保留 A. B. C. D.；答案区和解析区保持清晰；复杂整卷真题可以先导入，再进入核对页修正。",
            sample = null
        )
    }
}

@Composable
private fun FormatSection(
    title: String,
    body: String,
    sample: String?
) {
    GlassCard {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!sample.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            GlassCard {
                Text(
                    text = sample,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
