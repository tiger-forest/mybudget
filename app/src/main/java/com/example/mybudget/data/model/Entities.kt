package com.example.mybudget.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 消费标签实体
 * @param showCount 核心特性：是否在统计页面显示该标签的消费次数。默认 false（如三餐无需展示，谷子/奶茶等可手动开启）
 */
@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String, // Emoji 或图标标识
    val colorHex: Long, // 颜色十六进制值
    val showCount: Boolean = false,
    val isDefault: Boolean = false
)

/**
 * 单笔消费记录
 */
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val tagId: Long,
    val tagName: String,
    val tagIcon: String,
    val tagColorHex: Long,
    val dateMillis: Long,
    val note: String = ""
)

/**
 * 生活费月度预算配置（全局默认基准）
 */
@Entity(tableName = "budget_preference")
data class BudgetPreference(
    @PrimaryKey
    val id: Int = 1,
    val monthlyBudget: Double = 2000.0 // 默认每月生活费基准 2000 元
)

/**
 * 特定月份的生活费预算（按年月单独自定义）
 */
@Entity(
    tableName = "monthly_budgets",
    primaryKeys = ["year", "month"]
)
data class MonthlyBudgetEntity(
    val year: Int,
    val month: Int,
    val budget: Double
)
