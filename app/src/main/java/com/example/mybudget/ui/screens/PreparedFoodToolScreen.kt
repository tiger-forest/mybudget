package com.example.mybudget.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybudget.data.model.PreparedFoodEntity
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * 预制食品与自备餐成本管理工具
 */
@Composable
fun PreparedFoodToolScreen(
    foodList: List<PreparedFoodEntity>,
    onAddFood: (name: String, price: Double, unit: String, category: String, note: String) -> Unit,
    onUpdateFood: (PreparedFoodEntity) -> Unit,
    onDeleteFood: (PreparedFoodEntity) -> Unit,
    onRecordMealExpense: (amount: Double, note: String, onSuccess: () -> Unit) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var selectedTab by remember { mutableIntStateOf(0) }
    // 选购中的食材及份数映射 (foodId -> count)
    val selectedCounts = remember { mutableStateMapOf<Long, Int>() }

    // 食材增删改弹窗状态
    var showAddFoodDialog by remember { mutableStateOf(false) }
    var foodToEdit by remember { mutableStateOf<PreparedFoodEntity?>(null) }
    var foodToDelete by remember { mutableStateOf<PreparedFoodEntity?>(null) }

    // 记账确认对话框状态
    var showRecordConfirmDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // 计算当前自备餐总金额与总件数
    val currentMealCost = foodList.sumOf { food ->
        val count = selectedCounts[food.id] ?: 0
        food.price * count
    }
    val totalItemCount = selectedCounts.values.sum()

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶栏：返回按钮与工具标题
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回小工具列表"
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "预制食品与餐费管理",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "不去食堂时代餐囤货成本核算与记账",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Tab 导航：这顿饭成本核算 vs 食品价格库
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Fastfood,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "这顿饭成本核算")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "食品价格库 (${foodList.size})")
                        }
                    }
                )
            }

            // 内容区域
            when (selectedTab) {
                0 -> MealCostCalculatorContent(
                    foodList = foodList,
                    selectedCounts = selectedCounts,
                    currentMealCost = currentMealCost,
                    totalItemCount = totalItemCount,
                    onCountChange = { foodId, newCount ->
                        if (newCount <= 0) {
                            selectedCounts.remove(foodId)
                        } else {
                            selectedCounts[foodId] = newCount
                        }
                    },
                    onReset = { selectedCounts.clear() },
                    onRecordClick = {
                        if (currentMealCost > 0) {
                            showRecordConfirmDialog = true
                        }
                    },
                    onNavigateToLibrary = { selectedTab = 1 }
                )
                1 -> FoodPriceLibraryContent(
                    foodList = foodList,
                    onAddNew = { showAddFoodDialog = true },
                    onEdit = { foodToEdit = it },
                    onDelete = { foodToDelete = it }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }

    // 新增/编辑食品弹窗
    if (showAddFoodDialog || foodToEdit != null) {
        val initialFood = foodToEdit
        AddOrEditPreparedFoodDialog(
            initialFood = initialFood,
            onDismiss = {
                showAddFoodDialog = false
                foodToEdit = null
            },
            onConfirm = { name, price, unit, category, note ->
                if (initialFood != null) {
                    onUpdateFood(
                        initialFood.copy(
                            name = name,
                            price = price,
                            unit = unit,
                            category = category,
                            note = note
                        )
                    )
                } else {
                    onAddFood(name, price, unit, category, note)
                }
                showAddFoodDialog = false
                foodToEdit = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(if (initialFood != null) "已更新食品信息" else "已添加新食品")
                }
            }
        )
    }

    // 删除食品确认弹窗
    foodToDelete?.let { food ->
        AlertDialog(
            onDismissRequest = { foodToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("确认删除食品？") },
            text = { Text("确定要从食品库中删除“${food.name}”吗？此操作不会影响已记入账本的历史支出。") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteFood(food)
                        selectedCounts.remove(food.id)
                        foodToDelete = null
                        coroutineScope.launch { snackbarHostState.showSnackbar("已删除食品") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { foodToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }

    // 记入账本确认弹窗
    if (showRecordConfirmDialog) {
        val selectedDetails = foodList
            .filter { (selectedCounts[it.id] ?: 0) > 0 }
            .joinToString(", ") { "${it.name}x${selectedCounts[it.id]}" }
        val generatedNote = "自备餐：$selectedDetails"

        AlertDialog(
            onDismissRequest = { showRecordConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("确认记入账本？") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "即将向「三餐」标签记入这笔自备餐开销：",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "¥ ${String.format(Locale.getDefault(), "%.2f", currentMealCost)}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "备注内容：$generatedNote",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRecordConfirmDialog = false
                        onRecordMealExpense(currentMealCost, generatedNote) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("已成功记入今日三餐账单！")
                            }
                            selectedCounts.clear()
                        }
                    }
                ) {
                    Text("确认记账")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecordConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 模块一：这顿饭成本核算
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MealCostCalculatorContent(
    foodList: List<PreparedFoodEntity>,
    selectedCounts: Map<Long, Int>,
    currentMealCost: Double,
    totalItemCount: Int,
    onCountChange: (Long, Int) -> Unit,
    onReset: () -> Unit,
    onRecordClick: () -> Unit,
    onNavigateToLibrary: () -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf("全部") }
    val categories = remember(foodList) {
        listOf("全部") + foodList.map { it.category }.filter { it.isNotBlank() }.distinct()
    }

    val filteredList = remember(foodList, selectedCategoryFilter) {
        if (selectedCategoryFilter == "全部") foodList
        else foodList.filter { it.category == selectedCategoryFilter }
    }

    // 食堂基准均价（一般大学一顿饭约 15 元）
    val canteenBaseline = 15.0
    val savedAmount = canteenBaseline - currentMealCost

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // 顶部核心卡片：当前餐费总额与省钱估算
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "这顿饭总成本",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "¥ ",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%.2f", currentMealCost),
                                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // 已选件数指示胶囊
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (totalItemCount == 0) "未点选" else "已选 $totalItemCount 样",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // 对比食堂均价省钱提示
                    if (currentMealCost > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = if (savedAmount >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (savedAmount >= 0) {
                                    "对比食堂基准 (~¥15.00)，本顿节约约 ¥${String.format(Locale.getDefault(), "%.2f", savedAmount)}"
                                } else {
                                    "这顿大餐支出超出食堂基准 ¥${String.format(Locale.getDefault(), "%.2f", -savedAmount)}"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = if (savedAmount >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Text(
                            text = "💡 点击下方食品列表的「+」添加你这顿吃的干粮或泡面，即时算出这顿饭花销",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }

                    // 底部操作按钮：一键记入账本 & 清空重置
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onRecordClick,
                            enabled = currentMealCost > 0,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "一键记入账本")
                        }

                        OutlinedButton(
                            onClick = onReset,
                            enabled = totalItemCount > 0,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "重置")
                        }
                    }
                }
            }
        }

        // 分类筛选 Chips
        if (categories.size > 1) {
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategoryFilter == category,
                            onClick = { selectedCategoryFilter = category },
                            label = { Text(text = category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }

        // 食品挑选列表
        if (foodList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "食品库暂无记录",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "请前往「食品价格库」添加你经常备用的食品单价",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Button(onClick = onNavigateToLibrary) {
                            Text("前往录入食品")
                        }
                    }
                }
            }
        } else {
            items(filteredList, key = { it.id }) { food ->
                val count = selectedCounts[food.id] ?: 0
                FoodSelectorItemCard(
                    food = food,
                    count = count,
                    onCountChange = { onCountChange(food.id, it) }
                )
            }
        }
    }
}

/**
 * 食材挑选条目卡片
 */
@Composable
private fun FoodSelectorItemCard(
    food: PreparedFoodEntity,
    count: Int,
    onCountChange: (Int) -> Unit
) {
    val isSelected = count > 0
    val cardColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        else MaterialTheme.colorScheme.surface,
        label = "foodCardColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = food.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = food.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "¥ ${String.format(Locale.getDefault(), "%.2f", food.price)}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " / ${food.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (food.note.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "· ${food.note}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 数量控制器
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isSelected) {
                    IconButton(
                        onClick = { onCountChange(count - 1) },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "减少",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }

                IconButton(
                    onClick = { onCountChange(count + 1) },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "增加",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

/**
 * 模块二：预制食品价格库管理
 */
@Composable
private fun FoodPriceLibraryContent(
    foodList: List<PreparedFoodEntity>,
    onAddNew: () -> Unit,
    onEdit: (PreparedFoodEntity) -> Unit,
    onDelete: (PreparedFoodEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // 顶部操作与说明卡片
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "食品单价库",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "提前记录囤货均价，算账更快捷准确",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Button(
                        onClick = onAddNew,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "添加食品")
                    }
                }
            }
        }

        items(foodList, key = { it.id }) { food ->
            FoodLibraryItemCard(
                food = food,
                onEdit = { onEdit(food) },
                onDelete = { onDelete(food) }
            )
        }
    }
}

/**
 * 食品价格库条目卡片
 */
@Composable
private fun FoodLibraryItemCard(
    food: PreparedFoodEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = food.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = food.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "¥ ${String.format(Locale.getDefault(), "%.2f", food.price)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " / ${food.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                if (food.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "备注：${food.note}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * 预制食品添加与编辑弹窗
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddOrEditPreparedFoodDialog(
    initialFood: PreparedFoodEntity?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, price: Double, unit: String, category: String, note: String) -> Unit
) {
    val isEdit = initialFood != null
    var name by remember { mutableStateOf(initialFood?.name ?: "") }
    var priceText by remember { mutableStateOf(initialFood?.let { String.format(Locale.getDefault(), "%.2f", it.price) } ?: "") }
    var unit by remember { mutableStateOf(initialFood?.unit ?: "包") }
    var category by remember { mutableStateOf(initialFood?.category ?: "速食主食") }
    var note by remember { mutableStateOf(initialFood?.note ?: "") }

    val presetUnits = listOf("包", "桶", "块", "根", "个", "盒", "袋", "份", "瓶")
    val presetCategories = listOf("速食主食", "佐餐加料", "应急代餐", "冲饮零食", "其他")

    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (isEdit) "编辑食品单价" else "录入新食品") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("食品名称 (如: 压缩饼干)") },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = {
                        priceText = it
                        priceError = false
                    },
                    label = { Text("单价金额 (元)") },
                    placeholder = { Text("例如 1.8") },
                    isError = priceError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 常用计价单位快捷选择
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "计量单位 (当前: $unit)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        presetUnits.forEach { u ->
                            FilterChip(
                                selected = unit == u,
                                onClick = { unit = u },
                                label = { Text(u) },
                                modifier = Modifier.height(32.dp)
                            )
                        }
                    }
                }

                // 分类快捷选择
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "食品分类 (当前: $category)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        presetCategories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat) },
                                modifier = Modifier.height(32.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注说明 (可选，如: 整箱批发均价)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull()
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    if (price == null || price <= 0) {
                        priceError = true
                        return@Button
                    }
                    onConfirm(name.trim(), price, unit.trim(), category.trim(), note.trim())
                }
            ) {
                Text(if (isEdit) "保存" else "添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
