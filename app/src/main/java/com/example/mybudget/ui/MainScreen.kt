package com.example.mybudget.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mybudget.ui.components.AddExpenseSheet
import com.example.mybudget.ui.screens.HomeScreen
import com.example.mybudget.ui.screens.StatsScreen
import com.example.mybudget.ui.screens.TagManageScreen
import com.example.mybudget.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

sealed class NavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : NavTab("账本", Icons.Filled.Home, Icons.Outlined.Home)
    data object Stats : NavTab("统计", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    data object Tags : NavTab("管理", Icons.Filled.Label, Icons.Outlined.Label)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val allTags by viewModel.allTags.collectAsState()

    var currentTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(NavTab.Home, NavTab.Stats, NavTab.Tags)

    var showAddExpenseSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentTabIndex) {
                            0 -> "大学生活费小账本"
                            1 -> "开销维度分析"
                            else -> "生活费与标签管理"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(tonalElevation = 6.dp) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = currentTabIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(text = tab.title) }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddExpenseSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "记一笔")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTabIndex) {
                0 -> HomeScreen(
                    uiState = uiState,
                    onDeleteExpense = { viewModel.deleteExpense(it) },
                    onUpdateMonthlyBudget = { year, month, amount ->
                        viewModel.setMonthlyBudget(year, month, amount)
                    }
                )
                1 -> StatsScreen(
                    uiState = uiState,
                    onSelectTimeRange = { viewModel.selectTimeRange(it) },
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                    onSelectYearMonth = { year, month -> viewModel.selectYearMonth(year, month) },
                    onResetToCurrentMonth = { viewModel.resetToCurrentMonth() },
                    onSetMonthlyBudget = { year, month, amount ->
                        viewModel.setMonthlyBudget(year, month, amount)
                    }
                )
                2 -> TagManageScreen(
                    uiState = uiState,
                    onSetMonthlyBudget = { year, month, amount ->
                        viewModel.setMonthlyBudget(year, month, amount)
                    },
                    onDeleteMonthlyBudget = { year, month ->
                        viewModel.deleteMonthlyBudget(year, month)
                    },
                    onSetDefaultBudget = { amount ->
                        viewModel.setDefaultBudget(amount)
                    },
                    onToggleShowCount = { tagId, showCount -> viewModel.toggleTagShowCount(tagId, showCount) },
                    onAddTag = { name, icon, colorHex, showCount ->
                        viewModel.addTag(name, icon, colorHex, showCount)
                    },
                    onUpdateTag = { viewModel.updateTag(it) },
                    onDeleteTag = { viewModel.deleteTag(it) }
                )
            }
        }
    }

    if (showAddExpenseSheet) {
        AddExpenseSheet(
            sheetState = sheetState,
            tags = allTags,
            onDismiss = {
                coroutineScope.launch { sheetState.hide() }
                showAddExpenseSheet = false
            },
            onConfirm = { amount, tag, note, dateMillis ->
                viewModel.addExpense(amount, tag, note, dateMillis)
                coroutineScope.launch { sheetState.hide() }
                showAddExpenseSheet = false
            }
        )
    }
}
