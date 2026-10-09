package com.example.mybudget.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mybudget.data.model.MonthlyBudgetEntity
import com.example.mybudget.data.model.ExpenseEntity
import com.example.mybudget.data.model.TagEntity
import com.example.mybudget.data.repository.BudgetRepository
import com.example.mybudget.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TimeRange(val title: String) {
    TODAY("本日"),
    THIS_WEEK("本周"),
    THIS_MONTH("月度")
}

data class TagStatItem(
    val tagId: Long,
    val tagName: String,
    val tagIcon: String,
    val tagColorHex: Long,
    val totalAmount: Double,
    val count: Int,
    val showCount: Boolean, // 核心需求：该标签是否在统计中展示次数
    val percentage: Float
)

data class DailyExpenseGroup(
    val dateText: String,
    val dayTotal: Double,
    val items: List<ExpenseEntity>
)

data class UiState(
    val monthlyBudget: Double = 2000.0, // 当前本月生效预算
    val defaultBudget: Double = 2000.0, // 默认每月生活费基准
    val isCurrentMonthCustomBudget: Boolean = false, // 本月是否已独立设置
    val currentYear: Int = 2026,
    val currentMonth: Int = 9,
    val monthSpent: Double = 0.0,
    val monthRemaining: Double = 2000.0,
    val budgetProgress: Float = 0f,
    val todaySpent: Double = 0.0,
    val dailyAvailable: Double = 0.0,
    val selectedTimeRange: TimeRange = TimeRange.THIS_MONTH,
    val selectedYear: Int = 2026,
    val selectedMonth: Int = 9,
    val selectedMonthText: String = "2026年9月",
    val selectedMonthBudget: Double = 2000.0, // 统计页选中的月份预算
    val isSelectedMonthCustomBudget: Boolean = false, // 统计页选中的月份是否已独立设置
    val isCurrentMonth: Boolean = true,
    val availableRecordedMonths: List<Pair<Int, Int>> = emptyList(),
    val periodTotal: Double = 0.0,
    val tagStats: List<TagStatItem> = emptyList(),
    val expenseGroups: List<DailyExpenseGroup> = emptyList(),
    val allTags: List<TagEntity> = emptyList(),
    val customMonthlyBudgets: List<MonthlyBudgetEntity> = emptyList() // 所有已单独设定的月份预算
)

class MainViewModel(
    private val repository: BudgetRepository
) : ViewModel() {

    private val _selectedTimeRange = MutableStateFlow(TimeRange.THIS_MONTH)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange

    private val _selectedYearMonth = MutableStateFlow(DateTimeUtils.getCurrentYearMonth())
    val selectedYearMonth: StateFlow<Pair<Int, Int>> = _selectedYearMonth

    val allTags = repository.allTags.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _budgetFlow = combine(repository.budget, repository.allMonthlyBudgets) { pref, monthlyList ->
        Pair(pref, monthlyList)
    }

    val uiState: StateFlow<UiState> = combine(
        repository.allExpenses,
        repository.allTags,
        _budgetFlow,
        _selectedTimeRange,
        _selectedYearMonth
    ) { expenses, tags, (budgetPref, monthlyBudgets), range, (selYear, selMonth) ->
        val defaultBaseline = budgetPref?.monthlyBudget ?: 2000.0
        val (curYear, curMonth) = DateTimeUtils.getCurrentYearMonth()

        // 1. 本月生活费预算与开销计算（优先取本月专属设定，未设定则取默认基准）
        val currentMonthCustom = monthlyBudgets.find { it.year == curYear && it.month == curMonth }
        val curMonthBudget = currentMonthCustom?.budget ?: defaultBaseline
        val isCurMonthCustom = currentMonthCustom != null

        val (curMonthStart, curMonthEnd) = DateTimeUtils.getStartAndEndOfMonth(curYear, curMonth)
        val currentMonthExpenses = expenses.filter { it.dateMillis in curMonthStart..curMonthEnd }
        val monthSpent = currentMonthExpenses.sumOf { it.amount }
        val monthRemaining = curMonthBudget - monthSpent
        val budgetProgress = if (curMonthBudget > 0) (monthSpent / curMonthBudget).toFloat().coerceIn(0f, 1f) else 0f

        // 2. 今日与建议日均计算
        val (todayStart, todayEnd) = DateTimeUtils.getStartAndEndOfToday()
        val todayExpenses = expenses.filter { it.dateMillis in todayStart..todayEnd }
        val todaySpent = todayExpenses.sumOf { it.amount }
        val remainingDays = DateTimeUtils.getRemainingDaysInMonth()
        val dailyAvailable = if (remainingDays > 0) (monthRemaining.coerceAtLeast(0.0) / remainingDays) else 0.0

        // 3. 统计维度数据过滤（本日/本周/指定月度）
        val (rangeStart, rangeEnd) = when (range) {
            TimeRange.TODAY -> DateTimeUtils.getStartAndEndOfToday()
            TimeRange.THIS_WEEK -> DateTimeUtils.getStartAndEndOfWeek()
            TimeRange.THIS_MONTH -> DateTimeUtils.getStartAndEndOfMonth(selYear, selMonth)
        }
        val periodExpenses = expenses.filter { it.dateMillis in rangeStart..rangeEnd }
        val periodTotal = periodExpenses.sumOf { it.amount }

        // 统计选定月份的预算配置
        val selectedMonthCustom = monthlyBudgets.find { it.year == selYear && it.month == selMonth }
        val selectedMonthBudget = selectedMonthCustom?.budget ?: defaultBaseline
        val isSelMonthCustom = selectedMonthCustom != null

        // 4. 按标签分类汇总（处理标签次数开关配置与标签最新属性）
        val tagMap = tags.associateBy { it.id }
        val groupedByTag = periodExpenses.groupBy { it.tagId }
        val stats = groupedByTag.map { (tagId, expList) ->
            val tag = tagMap[tagId]
            val total = expList.sumOf { it.amount }
            val count = expList.size
            val tagName = tag?.name ?: expList.firstOrNull()?.tagName ?: "其他"
            val tagIcon = tag?.icon ?: expList.firstOrNull()?.tagIcon ?: "💰"
            val tagColor = tag?.colorHex ?: expList.firstOrNull()?.tagColorHex ?: 0xFF9E9E9E
            // 核心：若标签配置了 showCount，则为 true；否则默认 false
            val showCount = tag?.showCount ?: false
            val percent = if (periodTotal > 0) (total / periodTotal).toFloat() else 0f

            TagStatItem(
                tagId = tagId,
                tagName = tagName,
                tagIcon = tagIcon,
                tagColorHex = tagColor,
                totalAmount = total,
                count = count,
                showCount = showCount,
                percentage = percent
            )
        }.sortedByDescending { it.totalAmount }

        // 5. 按天分组流水列表
        val groupedByDay = expenses.groupBy { DateTimeUtils.formatFullDate(it.dateMillis) }
        val expenseGroups = groupedByDay.map { (_, list) ->
            val firstMillis = list.first().dateMillis
            DailyExpenseGroup(
                dateText = DateTimeUtils.formatDate(firstMillis),
                dayTotal = list.sumOf { it.amount },
                items = list.sortedByDescending { it.dateMillis }
            )
        }

        // 6. 历史有账单月份列表汇总
        val recordedMonths = expenses
            .map { DateTimeUtils.getYearAndMonth(it.dateMillis) }
            .plus(Pair(curYear, curMonth))
            .distinct()
            .sortedWith(compareByDescending<Pair<Int, Int>> { it.first }.thenByDescending { it.second })

        val isCurrentMonth = selYear == curYear && selMonth == curMonth

        UiState(
            monthlyBudget = curMonthBudget,
            defaultBudget = defaultBaseline,
            isCurrentMonthCustomBudget = isCurMonthCustom,
            currentYear = curYear,
            currentMonth = curMonth,
            monthSpent = monthSpent,
            monthRemaining = monthRemaining,
            budgetProgress = budgetProgress,
            todaySpent = todaySpent,
            dailyAvailable = dailyAvailable,
            selectedTimeRange = range,
            selectedYear = selYear,
            selectedMonth = selMonth,
            selectedMonthText = DateTimeUtils.formatYearMonth(selYear, selMonth),
            selectedMonthBudget = selectedMonthBudget,
            isSelectedMonthCustomBudget = isSelMonthCustom,
            isCurrentMonth = isCurrentMonth,
            availableRecordedMonths = recordedMonths,
            periodTotal = periodTotal,
            tagStats = stats,
            expenseGroups = expenseGroups,
            allTags = tags,
            customMonthlyBudgets = monthlyBudgets
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState()
    )

    fun selectTimeRange(range: TimeRange) {
        _selectedTimeRange.value = range
    }

    fun previousMonth() {
        val (year, month) = _selectedYearMonth.value
        if (month == 1) {
            _selectedYearMonth.value = Pair(year - 1, 12)
        } else {
            _selectedYearMonth.value = Pair(year, month - 1)
        }
    }

    fun nextMonth() {
        val (year, month) = _selectedYearMonth.value
        if (month == 12) {
            _selectedYearMonth.value = Pair(year + 1, 1)
        } else {
            _selectedYearMonth.value = Pair(year, month + 1)
        }
    }

    fun selectYearMonth(year: Int, month: Int) {
        _selectedYearMonth.value = Pair(year, month)
    }

    fun resetToCurrentMonth() {
        _selectedYearMonth.value = DateTimeUtils.getCurrentYearMonth()
    }

    fun addExpense(
        amount: Double,
        tag: TagEntity,
        note: String,
        dateMillis: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.insertExpense(
                ExpenseEntity(
                    amount = amount,
                    tagId = tag.id,
                    tagName = tag.name,
                    tagIcon = tag.icon,
                    tagColorHex = tag.colorHex,
                    dateMillis = dateMillis,
                    note = note
                )
            )
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    /**
     * 设定指定年月的专属生活费预算
     */
    fun setMonthlyBudget(year: Int, month: Int, amount: Double) {
        viewModelScope.launch {
            repository.setMonthlyBudget(year, month, amount)
        }
    }

    /**
     * 删除指定年月的专属预算（恢复使用默认基准）
     */
    fun deleteMonthlyBudget(year: Int, month: Int) {
        viewModelScope.launch {
            repository.deleteMonthlyBudget(year, month)
        }
    }

    /**
     * 修改全局默认每月生活费基准
     */
    fun setDefaultBudget(amount: Double) {
        viewModelScope.launch {
            repository.setBudget(amount)
        }
    }

    /**
     * 便捷方法：修改本月生活费预算
     */
    fun updateMonthlyBudget(amount: Double) {
        val (curYear, curMonth) = DateTimeUtils.getCurrentYearMonth()
        setMonthlyBudget(curYear, curMonth, amount)
    }

    fun toggleTagShowCount(tagId: Long, showCount: Boolean) {
        viewModelScope.launch {
            repository.updateTagShowCount(tagId, showCount)
        }
    }

    fun addTag(name: String, icon: String, colorHex: Long, showCount: Boolean) {
        viewModelScope.launch {
            repository.insertTag(
                TagEntity(
                    name = name,
                    icon = icon,
                    colorHex = colorHex,
                    showCount = showCount,
                    isDefault = false
                )
            )
        }
    }

    fun updateTag(tag: TagEntity) {
        viewModelScope.launch {
            repository.updateTag(tag)
        }
    }

    fun deleteTag(tag: TagEntity) {
        viewModelScope.launch {
            repository.deleteTag(tag)
        }
    }
}

class MainViewModelFactory(
    private val repository: BudgetRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
