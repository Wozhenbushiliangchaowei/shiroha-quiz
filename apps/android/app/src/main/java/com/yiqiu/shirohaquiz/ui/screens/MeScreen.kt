package com.yiqiu.shirohaquiz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing

@Composable
fun MeScreen() {
    Column(
        modifier = Modifier.padding(ShirohaSpacing.Xl),
        verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
    ) {
        ShirohaHeader(
            kicker = "Me",
            title = "设置与资料",
            subtitle = "后面这里会接入导出、备份、解析策略、主题和版本信息。"
        )
        GlassCard {
            Text(
                text = "当前阶段先把原生导入链、原生练习和原生考试主流程做稳，设置页暂时保持轻量占位。",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
