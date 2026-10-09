package com.example.mybudget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybudget.data.model.PreparedFoodEntity

enum class ToolType {
    PREPARED_FOOD
}

@Composable
fun ToolsScreen(
    foodList: List<PreparedFoodEntity>,
    onAddFood: (name: String, price: Double, unit: String, category: String, note: String) -> Unit,
    onUpdateFood: (PreparedFoodEntity) -> Unit,
    onDeleteFood: (PreparedFoodEntity) -> Unit,
    onRecordMealExpense: (amount: Double, note: String, onSuccess: () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTool by remember { mutableStateOf<ToolType?>(null) }

    if (activeTool == ToolType.PREPARED_FOOD) {
        PreparedFoodToolScreen(
            foodList = foodList,
            onAddFood = onAddFood,
            onUpdateFood = onUpdateFood,
            onDeleteFood = onDeleteFood,
            onRecordMealExpense = onRecordMealExpense,
            onBack = { activeTool = null },
            modifier = modifier
        )
    } else {
        ToolboxHome(
            foodCount = foodList.size,
            onOpenPreparedFood = { activeTool = ToolType.PREPARED_FOOD },
            modifier = modifier
        )
    }
}

@Composable
private fun ToolboxHome(
    foodCount: Int,
    onOpenPreparedFood: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // 顶部欢迎卡片
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "大学生活小工具箱",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "围绕大学生日常生活场景定制的各种贴心计算与管理工具",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // 第一个小工具：预制食品与自备餐成本管理
        item {
            ToolItemCard(
                title = "预制食品餐费管理",
                subtitle = "记录压缩饼干、泡面、加餐等自备囤货单价。不去食堂时随手点选食材，一键算出这顿饭花销并直接记入账本。",
                badgeText = "已收录 $foodCount 种食品",
                icon = Icons.Default.Fastfood,
                iconBackgroundColor = Color(0xFFFF7043),
                isAvailable = true,
                onClick = onOpenPreparedFood
            )
        }

        // 规划中的小工具卡片 2
        item {
            ToolItemCard(
                title = "学期生活费天数倒计时",
                subtitle = "根据放假回家倒计时，动态测算本月及后续每日平均可支配生活费指标，告别月底吃土。",
                badgeText = "规划中",
                icon = Icons.Default.HourglassTop,
                iconBackgroundColor = Color(0xFF42A5F5),
                isAvailable = false,
                onClick = {}
            )
        }

        // 规划中的小工具卡片 3
        item {
            ToolItemCard(
                title = "大学恩格尔系数分析",
                subtitle = "测算饮食餐饮占每月总开销比例，评估当月是在享受美食还是在冲动消费。",
                badgeText = "规划中",
                icon = Icons.Default.Calculate,
                iconBackgroundColor = Color(0xFFAB47BC),
                isAvailable = false,
                onClick = {}
            )
        }
    }
}

@Composable
private fun ToolItemCard(
    title: String,
    subtitle: String,
    badgeText: String,
    icon: ImageVector,
    iconBackgroundColor: Color,
    isAvailable: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = isAvailable, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAvailable) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isAvailable) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBackgroundColor.copy(alpha = if (isAvailable) 0.18f else 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isAvailable) iconBackgroundColor else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isAvailable) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isAvailable) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = if (isAvailable) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isAvailable) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.outline,
                    lineHeight = 18.sp
                )
            }

            if (isAvailable) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "进入工具",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
