package com.example.mybudget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.mybudget.ui.components.DonutChart
import com.example.mybudget.ui.components.MonthlyBalanceSheet
import com.example.mybudget.ui.viewmodel.TagStatItem
import com.example.mybudget.ui.viewmodel.TimeRange
import com.example.mybudget.ui.viewmodel.UiState
import com.example.mybudget.util.DateTimeUtils
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    uiState: UiState,
    onSelectTimeRange: (TimeRange) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectYearMonth: (Int, Int) -> Unit,
    onResetToCurrentMonth: () -> Unit,
    onSetMonthlyBudget: (year: Int, month: Int, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMonthPickerDialog by remember { mutableStateOf(false) }
    var showEditSelectedMonthBudgetDialog by remember { mutableStateOf(false) }
    var showMonthlyBalanceSheet by remember { mutableStateOf(false) }
    val balanceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val timeRanges = listOf(TimeRange.TODAY, TimeRange.THIS_WEEK, TimeRange.THIS_MONTH)
    val selectedIndex = timeRanges.indexOf(uiState.selectedTimeRange)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // 时间维度切换选项卡（本日 / 本周 / 月度）
            TabRow(
                selectedTabIndex = selectedIndex,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                timeRanges.forEachIndexed { index, range ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = { onSelectTimeRange(range) },
                        text = {
                            Text(
                                text = range.title,
                                fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        item {
            // 每月结余一览入口按钮卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { showMonthlyBalanceSheet = true },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "每月结余一览",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (uiState.allMonthBalances.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "共${uiState.allMonthBalances.size}个月",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                text = if (uiState.allMonthBalances.isNotEmpty()) {
                                    val isPos = uiState.totalHistoryBalance >= 0
                                    "历史累计${if (isPos) "节余" else "超支"} ¥${String.format("%.2f", abs(uiState.totalHistoryBalance))} · 点击查看明细"
                                } else {
                                    "查看所有历史月份的预算与结余对比"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "查看每月结余",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 当选择月度维度时，显示月份导航切换与该月预算概览
        if (uiState.selectedTimeRange == TimeRange.THIS_MONTH) {
            item {
                val monthBalance = uiState.selectedMonthBudget - uiState.periodTotal
                val isOverBudget = monthBalance < 0
                val progress = if (uiState.selectedMonthBudget > 0) {
                    (uiState.periodTotal / uiState.selectedMonthBudget).toFloat().coerceIn(0f, 1f)
                } else 0f

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(onClick = onPreviousMonth) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "上一月",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // 点击中间年月可弹出月份快捷选择器
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                    .clickable { showMonthPickerDialog = true }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = uiState.selectedMonthText,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (uiState.isCurrentMonth) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "(本月)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "选择月份",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(onClick = onNextMonth) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "下一月",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // 该月预算与开销执行对比条
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "当月预算",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (uiState.isSelectedMonthCustomBudget) "专属" else "默认",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (uiState.isSelectedMonthCustomBudget) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                }
                                Text(
                                    text = "¥${String.format("%.0f", uiState.selectedMonthBudget)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "当月支出",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = "¥${String.format("%.2f", uiState.periodTotal)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isOverBudget) "超支" else "结余",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = if (isOverBudget) "-¥${String.format("%.2f", -monthBalance)}" else "¥${String.format("%.2f", monthBalance)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = { showEditSelectedMonthBudgetDialog = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "修改当月预算",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // 如果当前选中的不是当月，提供一键回到当月的小按钮
                        if (!uiState.isCurrentMonth) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp, bottom = 2.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                AssistChip(
                                    onClick = onResetToCurrentMonth,
                                    label = { Text("回到当前月", fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Today,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 环形开销图表区域
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val chartTitle = when (uiState.selectedTimeRange) {
                        TimeRange.TODAY -> "本日开销构成"
                        TimeRange.THIS_WEEK -> "本周开销构成"
                        TimeRange.THIS_MONTH -> "${uiState.selectedMonthText}开销构成"
                    }

                    Text(
                        text = chartTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "总支出 ¥${String.format("%.2f", uiState.periodTotal)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DonutChart(
                        stats = uiState.tagStats,
                        totalAmount = uiState.periodTotal
                    )
                }
            }
        }

        // 分类支出排行
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "分类支出排行",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "次数仅在开启标签时展示",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        if (uiState.tagStats.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📊", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        val emptyText = when (uiState.selectedTimeRange) {
                            TimeRange.TODAY -> "本日还没有任何开销"
                            TimeRange.THIS_WEEK -> "本周还没有任何开销"
                            TimeRange.THIS_MONTH -> "${uiState.selectedMonthText}没有任何开销"
                        }
                        Text(
                            text = emptyText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            itemsIndexed(uiState.tagStats, key = { _, item -> item.tagId }) { index, item ->
                StatRankItemCard(
                    rank = index + 1,
                    stat = item
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // 月份选择对话框
    if (showMonthPickerDialog) {
        MonthPickerDialog(
            currentYear = uiState.selectedYear,
            currentMonth = uiState.selectedMonth,
            availableRecordedMonths = uiState.availableRecordedMonths,
            onDismiss = { showMonthPickerDialog = false },
            onSelect = { year, month ->
                onSelectYearMonth(year, month)
                showMonthPickerDialog = false
            }
        )
    }

    // 修改所查看月份的预算对话框
    if (showEditSelectedMonthBudgetDialog) {
        var budgetInput by remember(uiState.selectedYear, uiState.selectedMonth, uiState.selectedMonthBudget) {
            mutableStateOf(uiState.selectedMonthBudget.toInt().toString())
        }

        AlertDialog(
            onDismissRequest = { showEditSelectedMonthBudgetDialog = false },
            title = {
                Text(
                    text = "设定${uiState.selectedYear}年${uiState.selectedMonth}月生活费",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (uiState.isSelectedMonthCustomBudget) "当前状态：已设置当月专属预算" else "当前状态：正在使用全局默认基准 (¥${String.format("%.0f", uiState.defaultBudget)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { if (it.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) budgetInput = it },
                        label = { Text("${uiState.selectedMonth}月预算金额 (元)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = budgetInput.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            onSetMonthlyBudget(uiState.selectedYear, uiState.selectedMonth, amount)
                        }
                        showEditSelectedMonthBudgetDialog = false
                    }
                ) {
                    Text("保存", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditSelectedMonthBudgetDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 每月结余一览底部抽屉面板
    if (showMonthlyBalanceSheet) {
        MonthlyBalanceSheet(
            sheetState = balanceSheetState,
            monthBalances = uiState.allMonthBalances,
            totalHistoryBudget = uiState.totalHistoryBudget,
            totalHistoryExpense = uiState.totalHistoryExpense,
            totalHistoryBalance = uiState.totalHistoryBalance,
            onDismiss = { showMonthlyBalanceSheet = false },
            onSelectMonth = { year, month ->
                onSelectTimeRange(TimeRange.THIS_MONTH)
                onSelectYearMonth(year, month)
            }
        )
    }
}

/**
 * 分类排行条目组件
 * 核心逻辑：若 stat.showCount 为 true，则展示消费次数（如“共 4 次”）；若为 false 则隐藏次数。
 */
@Composable
private fun StatRankItemCard(
    rank: Int,
    stat: TagStatItem
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 排名序号
                    Text(
                        text = "$rank",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (rank) {
                                1 -> Color(0xFFFFB300)
                                2 -> Color(0xFF78909C)
                                3 -> Color(0xFF8D6E63)
                                else -> MaterialTheme.colorScheme.outline
                            }
                        ),
                        modifier = Modifier.width(20.dp)
                    )

                    // 标签图标
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(stat.tagColorHex).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = stat.tagIcon, fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = stat.tagName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // 占比展示
                        Text(
                            text = "占比 ${(stat.percentage * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // 金额与消费次数（根据标签设定动态显隐）
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "¥${String.format("%.2f", stat.totalAmount)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 🎯 核心需求呈现：仅当该标签 showCount 为 true 时，才显示消费次数
                    if (stat.showCount) {
                        Text(
                            text = "共 ${stat.count} 次消费",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 进度条直观展示占比
            LinearProgressIndicator(
                progress = { stat.percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(stat.tagColorHex),
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MonthPickerDialog(
    currentYear: Int,
    currentMonth: Int,
    availableRecordedMonths: List<Pair<Int, Int>>,
    onDismiss: () -> Unit,
    onSelect: (year: Int, month: Int) -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(currentYear) }
    var selectedMonth by remember { mutableIntStateOf(currentMonth) }

    val currentYm = DateTimeUtils.getCurrentYearMonth()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "选择统计月份", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 如果有历史记账月份，提供快捷选择标签
                if (availableRecordedMonths.isNotEmpty()) {
                    Text(
                        text = "快速选择（历史账单月份）",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableRecordedMonths.forEach { (y, m) ->
                            val isSelected = selectedYear == y && selectedMonth == m
                            val isNow = y == currentYm.first && m == currentYm.second
                            val label = "${y}年${m}月" + if (isNow) "(本月)" else ""

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedYear = y
                                        selectedMonth = m
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // 年份选择
                Text(
                    text = "按年份与月份自选",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { selectedYear -= 1 }
                    ) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "前一年")
                    }

                    Text(
                        text = "${selectedYear} 年",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = { selectedYear += 1 }
                    ) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "后一年")
                    }
                }

                // 12 个月份
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    maxItemsInEachRow = 4
                ) {
                    (1..12).forEach { m ->
                        val isSelected = selectedMonth == m
                        val isNow = selectedYear == currentYm.first && m == currentYm.second

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else if (isNow) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else if (isNow) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedMonth = m },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${m}月",
                                fontSize = 13.sp,
                                fontWeight = if (isSelected || isNow) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else if (isNow) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSelect(selectedYear, selectedMonth) }
            ) {
                Text("确定", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
