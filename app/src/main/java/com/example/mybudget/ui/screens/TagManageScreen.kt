package com.example.mybudget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import com.example.mybudget.data.model.MonthlyBudgetEntity
import com.example.mybudget.data.model.TagEntity
import com.example.mybudget.ui.viewmodel.UiState

@Composable
fun TagManageScreen(
    uiState: UiState,
    onSetMonthlyBudget: (year: Int, month: Int, Double) -> Unit,
    onDeleteMonthlyBudget: (year: Int, month: Int) -> Unit,
    onSetDefaultBudget: (Double) -> Unit,
    onToggleShowCount: (Long, Boolean) -> Unit,
    onAddTag: (name: String, icon: String, colorHex: Long, showCount: Boolean) -> Unit,
    onUpdateTag: (TagEntity) -> Unit,
    onDeleteTag: (TagEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCurrentMonthBudgetDialog by remember { mutableStateOf(false) }
    var showDefaultBudgetDialog by remember { mutableStateOf(false) }
    var showManageAllBudgetsDialog by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var tagToEdit by remember { mutableStateOf<TagEntity?>(null) }
    var tagToDelete by remember { mutableStateOf<TagEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // 每月生活费预算设置卡片
            BudgetSettingCard(
                uiState = uiState,
                onEditCurrentMonth = { showCurrentMonthBudgetDialog = true },
                onEditDefault = { showDefaultBudgetDialog = true },
                onManageAllMonths = { showManageAllBudgetsDialog = true }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = "标签与次数设置",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "自定义标签可点击修改属性，支持统计频次开关",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Button(
                    onClick = { showAddTagDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "新增标签",
                        fontSize = 14.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // 标签列表
        items(uiState.allTags, key = { it.id }) { tag ->
            TagManageItemCard(
                tag = tag,
                onToggleShowCount = { isChecked -> onToggleShowCount(tag.id, isChecked) },
                onEdit = { tagToEdit = tag },
                onDelete = { tagToDelete = tag }
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // 修改当前月生活费预算对话框
    if (showCurrentMonthBudgetDialog) {
        var budgetInput by remember { mutableStateOf(uiState.monthlyBudget.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { showCurrentMonthBudgetDialog = false },
            title = {
                Text(
                    text = "设定${uiState.currentYear}年${uiState.currentMonth}月生活费",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (uiState.isCurrentMonthCustomBudget) "当前状态：已设置当月独立预算" else "当前状态：正在使用全局默认基准",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { if (it.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) budgetInput = it },
                        label = { Text("本月预算 (元)") },
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
                            onSetMonthlyBudget(uiState.currentYear, uiState.currentMonth, amount)
                        }
                        showCurrentMonthBudgetDialog = false
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCurrentMonthBudgetDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 修改全局默认基准生活费对话框
    if (showDefaultBudgetDialog) {
        var budgetInput by remember { mutableStateOf(uiState.defaultBudget.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { showDefaultBudgetDialog = false },
            title = { Text("设定每月默认生活费基准", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "当新月份未单独设定生活费时，系统将自动采用此基准金额。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { if (it.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) budgetInput = it },
                        label = { Text("默认基准 (元)") },
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
                            onSetDefaultBudget(amount)
                        }
                        showDefaultBudgetDialog = false
                    }
                ) {
                    Text("保存基准")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDefaultBudgetDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 按月份管理与自定义预算对话框
    if (showManageAllBudgetsDialog) {
        ManageMonthlyBudgetsDialog(
            currentYear = uiState.currentYear,
            currentMonth = uiState.currentMonth,
            customBudgets = uiState.customMonthlyBudgets,
            defaultBudget = uiState.defaultBudget,
            onDismiss = { showManageAllBudgetsDialog = false },
            onSetBudget = { year, month, amount ->
                onSetMonthlyBudget(year, month, amount)
            },
            onDeleteBudget = { year, month ->
                onDeleteMonthlyBudget(year, month)
            },
            onEditDefault = {
                showManageAllBudgetsDialog = false
                showDefaultBudgetDialog = true
            }
        )
    }

    // 添加自定义标签对话框
    if (showAddTagDialog) {
        AddOrEditTagDialog(
            initialTag = null,
            onDismiss = { showAddTagDialog = false },
            onConfirm = { name, icon, colorHex, showCount ->
                onAddTag(name, icon, colorHex, showCount)
                showAddTagDialog = false
            }
        )
    }

    // 编辑自定义标签对话框
    tagToEdit?.let { tag ->
        AddOrEditTagDialog(
            initialTag = tag,
            onDismiss = { tagToEdit = null },
            onConfirm = { name, icon, colorHex, showCount ->
                onUpdateTag(
                    tag.copy(
                        name = name,
                        icon = icon,
                        colorHex = colorHex,
                        showCount = showCount
                    )
                )
                tagToEdit = null
            }
        )
    }

    // 删除标签确认对话框
    tagToDelete?.let { tag ->
        AlertDialog(
            onDismissRequest = { tagToDelete = null },
            title = { Text("删除标签？") },
            text = { Text("确定要删除「${tag.name}」标签吗？已记录的历史账单仍会保留。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTag(tag)
                        tagToDelete = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { tagToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun BudgetSettingCard(
    uiState: UiState,
    onEditCurrentMonth: () -> Unit,
    onEditDefault: () -> Unit,
    onManageAllMonths: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 核心区域：本月生活费与编辑
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onEditCurrentMonth() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "本月生活费 (${uiState.currentMonth}月)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.isCurrentMonthCustomBudget) "专属设定" else "默认基准",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.isCurrentMonthCustomBudget) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                        Text(
                            text = "¥${String.format("%.2f", uiState.monthlyBudget)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                IconButton(onClick = onEditCurrentMonth) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "修改本月生活费",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            // 底部快捷操作栏：默认基准设置 & 按月管理
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 默认基准入口
                TextButton(
                    onClick = onEditDefault,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "默认基准: ¥${String.format("%.0f", uiState.defaultBudget)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // 按月管理入口
                TextButton(
                    onClick = onManageAllMonths,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "按月管理",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun TagManageItemCard(
    tag: TagEntity,
    onToggleShowCount: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .then(if (!tag.isDefault) Modifier.clickable { onEdit() } else Modifier),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(tag.colorHex).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = tag.icon, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tag.name,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (tag.isDefault) {
                            Text(
                                text = "系统",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        } else {
                            Text(
                                text = "自定义(可修改)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Text(
                        text = if (tag.showCount) "已开启次数统计" else "默认隐藏次数",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (tag.showCount) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // 手动开启/隐藏该标签的统计次数
                Switch(
                    checked = tag.showCount,
                    onCheckedChange = onToggleShowCount,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // 非系统标签支持编辑属性与删除
                if (!tag.isDefault) {
                    Spacer(modifier = Modifier.width(2.dp))
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "修改标签属性",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "删除标签",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 标签编辑/新增对话框（包含丰富的图标分类库、自定义Emoji输入与多色彩调色板）
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddOrEditTagDialog(
    initialTag: TagEntity?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: String, colorHex: Long, showCount: Boolean) -> Unit
) {
    val isEditMode = initialTag != null

    var tagName by remember { mutableStateOf(initialTag?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(initialTag?.icon ?: "🎁") }
    var customEmojiInput by remember { mutableStateOf("") }
    var selectedColor by remember { mutableLongStateOf(initialTag?.colorHex ?: 0xFF7E57C2) }
    var showCount by remember { mutableStateOf(initialTag?.showCount ?: false) }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    val iconCategories = listOf(
        "全部" to listOf(
            "🍜", "🧋", "🍚", "🍔", "🍕", "🍲", "🍱", "🥪", "☕", "🍻", "🥤", "🍦", "🍰", "🍎", "🍉", "🥐", "🥩", "🥟", "🍳", "🍩", "🍺", "🍓", "🍫", "🍣",
            "🎁", "🛍️", "🛒", "💄", "👗", "👟", "💍", "🧸", "👜", "👒", "🕶️", "💅", "📦", "🎈", "🏷️", "💎", "⌚", "📱",
            "📚", "✏️", "💻", "🎓", "🎒", "🔬", "🎨", "🎹", "📖", "📝", "🖨️", "🖊️", "📐", "🧪", "💡", "🏆", "📌",
            "🎮", "🎬", "🎤", "🎧", "📸", "🎟️", "🎪", "🎳", "♟️", "🎯", "🏄", "🛹", "⛺", "🎣", "🍿", "🎡", "🎸",
            "🚌", "🚇", "🚲", "🚗", "🚕", "✈️", "🚂", "🛵", "⛽", "🎫", "🧳", "🚢", "🛴",
            "🧻", "🏠", "💡", "🚿", "🧼", "🧹", "🧺", "🔌", "🔑", "🛏️", "💇", "🧴", "🪥", "✂️", "🪴",
            "💊", "🏥", "🏃", "🏋️", "🏀", "⚽", "🏸", "🏊", "🚴", "🧘", "🧗", "🥊", "🏓", "🩺", "🩹",
            "🐱", "🐶", "🐰", "🐹", "🌸", "🐾", "💰", "💳", "🧾", "📈", "🧧", "💸", "💖", "🎂", "💐"
        ),
        "餐饮美食" to listOf("🍜", "🧋", "🍚", "🍔", "🍕", "🍲", "🍱", "🥪", "☕", "🍻", "🥤", "🍦", "🍰", "🍎", "🍉", "🥐", "🥩", "🥟", "🍳", "🍩", "🍺", "🍓", "🍫", "🍣", "🥗", "🌭", "🍇"),
        "购物消费" to listOf("🎁", "🛍️", "🛒", "💄", "👗", "👟", "💍", "🧸", "👜", "👒", "🕶️", "💅", "📦", "🎈", "🏷️", "💎", "⌚", "📱", "👔", "👠"),
        "学习校园" to listOf("📚", "✏️", "💻", "🎓", "🎒", "🔬", "🎨", "🎹", "📖", "📝", "🖨️", "🖊️", "📐", "🧪", "💡", "🏆", "📌", "📜", "📋"),
        "休闲娱乐" to listOf("🎮", "🎬", "🎤", "🎧", "📸", "🎟️", "🎪", "🎳", "♟️", "🎯", "🏄", "🛹", "⛺", "🎣", "🍿", "🎡", "🎸", "🧩", "🎲", "🕹️"),
        "交通出行" to listOf("🚌", "🚇", "🚲", "🚗", "🚕", "✈️", "🚂", "🛵", "⛽", "🎫", "🧳", "🚢", "🛴", "🚊", "🚖", "🚁", "⛵"),
        "日用居家" to listOf("🧻", "🏠", "💡", "🚿", "🧼", "🧹", "🧺", "🔌", "🔑", "🛏️", "💇", "🧴", "🪥", "✂️", "🪴", "🛁", "🔧", "🪞", "🧲"),
        "运动健康" to listOf("💊", "🏥", "🏃", "🏋️", "🏀", "⚽", "🏸", "🏊", "🚴", "🧘", "🧗", "🥊", "🏓", "🩺", "🩹", "🎾", "🏐", "🥋"),
        "宠物其他" to listOf("🐱", "🐶", "🐰", "🐹", "🌸", "🐾", "💰", "💳", "🧾", "📈", "🧧", "💸", "💖", "🎂", "💐", "🪙")
    )

    val presetColors = listOf(
        0xFFFF7043, 0xFFFFB74D, 0xFFFFA726, 0xFFFFCA28,
        0xFF66BB6A, 0xFF26A69A, 0xFF2E7D32, 0xFF00ACC1,
        0xFF42A5F5, 0xFF5C6BC0, 0xFF7E57C2, 0xFFAB47BC,
        0xFFEC407A, 0xFFE91E63, 0xFFD32F2F, 0xFFFF5722,
        0xFF8D6E63, 0xFF78909C, 0xFF546E7A, 0xFF37474F
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditMode) "编辑自定义标签" else "添加自定义标签",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 当前标签预览
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(selectedColor).copy(alpha = 0.15f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(selectedColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = selectedIcon, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (tagName.isNotBlank()) tagName else "标签名称预览",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (showCount) "将在统计页展示消费频次" else "默认隐藏消费频次",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // 名称输入框
                OutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    label = { Text("标签名称（如：盲盒、健身、宠物）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // 图标选择分类与列表
                Text(
                    text = "选择图标（${iconCategories[selectedCategoryIndex].first}）",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                ScrollableTabRow(
                    selectedTabIndex = selectedCategoryIndex,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    iconCategories.forEachIndexed { idx, (catName, _) ->
                        Tab(
                            selected = selectedCategoryIndex == idx,
                            onClick = { selectedCategoryIndex = idx },
                            text = { Text(catName, fontSize = 12.sp) }
                        )
                    }
                }

                // 当前分类图标网格
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        iconCategories[selectedCategoryIndex].second.forEach { icon ->
                            val isSelected = selectedIcon == icon
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) Color(selectedColor).copy(alpha = 0.3f)
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) Color(selectedColor) else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedIcon = icon },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = icon, fontSize = 18.sp)
                            }
                        }
                    }
                }

                // 自定义Emoji输入
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customEmojiInput,
                        onValueChange = { input ->
                            customEmojiInput = input
                            if (input.isNotBlank()) {
                                selectedIcon = input.trim()
                            }
                        },
                        label = { Text("或直接输入任意Emoji") },
                        placeholder = { Text("如 🎯、🏎️、🧃") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // 颜色选择
                Text(
                    text = "选择主题色彩",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetColors.forEach { colorHex ->
                        val isSelected = selectedColor == colorHex
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorHex }
                        )
                    }
                }

                // 频次统计开关
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "统计中显示消费次数",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = "开启后统计分析页将展示该标签已消费了几次",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Switch(checked = showCount, onCheckedChange = { showCount = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tagName.isNotBlank()) {
                        onConfirm(tagName.trim(), selectedIcon, selectedColor, showCount)
                    }
                },
                enabled = tagName.isNotBlank()
            ) {
                Text(if (isEditMode) "保存修改" else "确认添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 按月份管理与自定义预算对话框
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ManageMonthlyBudgetsDialog(
    currentYear: Int,
    currentMonth: Int,
    customBudgets: List<MonthlyBudgetEntity>,
    defaultBudget: Double,
    onDismiss: () -> Unit,
    onSetBudget: (year: Int, month: Int, amount: Double) -> Unit,
    onDeleteBudget: (year: Int, month: Int) -> Unit,
    onEditDefault: () -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(currentYear) }
    var selectedMonth by remember { mutableIntStateOf(currentMonth) }

    val matchedExisting = customBudgets.find { it.year == selectedYear && it.month == selectedMonth }
    var budgetInput by remember(selectedYear, selectedMonth) {
        mutableStateOf(matchedExisting?.budget?.toInt()?.toString() ?: defaultBudget.toInt().toString())
    }

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
                Text(text = "按月份设定生活费", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 顶部默认基准说明与快捷修改入口
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "全局默认基准",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "¥${String.format("%.2f", defaultBudget)} / 月",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    TextButton(
                        onClick = onEditDefault,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("修改基准", fontSize = 12.sp)
                    }
                }

                // 年份选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYear -= 1 }) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "上一年")
                    }
                    Text(
                        text = "${selectedYear} 年",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = { selectedYear += 1 }) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "下一年")
                    }
                }

                // 12 个月份选择网格
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    maxItemsInEachRow = 4
                ) {
                    (1..12).forEach { m ->
                        val isSelected = selectedMonth == m
                        val hasCustom = customBudgets.any { it.year == selectedYear && it.month == m }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else if (hasCustom) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else if (hasCustom) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedMonth = m },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${m}月" + if (hasCustom && !isSelected) "★" else "",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected || hasCustom) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else if (hasCustom) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // 选中月份的预算设置输入框
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "设定 ${selectedYear}年${selectedMonth}月 预算",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (matchedExisting != null) {
                                TextButton(
                                    onClick = { onDeleteBudget(selectedYear, selectedMonth) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("恢复默认", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = budgetInput,
                            onValueChange = { if (it.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) budgetInput = it },
                            label = { Text("${selectedMonth}月生活费预算 (元)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val amount = budgetInput.toDoubleOrNull() ?: 0.0
                                if (amount > 0) {
                                    onSetBudget(selectedYear, selectedMonth, amount)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("保存此月份预算")
                        }
                    }
                }

                // 已单独设定的月份列表
                Text(
                    text = "已独立设定的月份（共 ${customBudgets.size} 个）",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (customBudgets.isEmpty()) {
                    Text(
                        text = "暂无独立设定的月份，所有月份均遵循默认基准 ¥${String.format("%.0f", defaultBudget)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    customBudgets.sortedWith(compareByDescending<MonthlyBudgetEntity> { it.year }.thenByDescending { it.month }).forEach { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${item.year}年${item.month}月",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "¥${String.format("%.2f", item.budget)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            selectedYear = item.year
                                            selectedMonth = item.month
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "编辑",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteBudget(item.year, item.month) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "删除并恢复默认",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭", fontWeight = FontWeight.Bold)
            }
        }
    )
}

