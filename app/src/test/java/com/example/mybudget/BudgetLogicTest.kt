package com.example.mybudget

import com.example.mybudget.data.model.ExpenseEntity
import com.example.mybudget.data.model.TagEntity
import com.example.mybudget.ui.viewmodel.TagStatItem
import com.example.mybudget.util.DateTimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetLogicTest {

    @Test
    fun testDateTimeUtilsRanges() {
        val (todayStart, todayEnd) = DateTimeUtils.getStartAndEndOfToday()
        assertTrue("todayEnd should be greater than todayStart", todayEnd > todayStart)

        val (weekStart, weekEnd) = DateTimeUtils.getStartAndEndOfWeek()
        assertTrue("weekEnd should be greater than weekStart", weekEnd > weekStart)

        val (monthStart, monthEnd) = DateTimeUtils.getStartAndEndOfMonth()
        assertTrue("monthEnd should be greater than monthStart", monthEnd > monthStart)

        val remainingDays = DateTimeUtils.getRemainingDaysInMonth()
        assertTrue("remainingDays should be between 1 and 31", remainingDays in 1..31)
    }

    @Test
    fun testTagShowCountStatLogic() {
        val mealTag = TagEntity(id = 1, name = "三餐", icon = "🍜", colorHex = 0xFFFF7043, showCount = false)
        val goodTag = TagEntity(id = 2, name = "谷子周边", icon = "🎁", colorHex = 0xFFAB47BC, showCount = true)

        val expenses = listOf(
            ExpenseEntity(id = 1, amount = 25.0, tagId = 1, tagName = "三餐", tagIcon = "🍜", tagColorHex = 0xFFFF7043, dateMillis = System.currentTimeMillis()),
            ExpenseEntity(id = 2, amount = 30.0, tagId = 1, tagName = "三餐", tagIcon = "🍜", tagColorHex = 0xFFFF7043, dateMillis = System.currentTimeMillis()),
            ExpenseEntity(id = 3, amount = 150.0, tagId = 2, tagName = "谷子周边", tagIcon = "🎁", tagColorHex = 0xFFAB47BC, dateMillis = System.currentTimeMillis())
        )

        val tags = listOf(mealTag, goodTag)
        val tagMap = tags.associateBy { it.id }
        val periodTotal = expenses.sumOf { it.amount }

        val stats = expenses.groupBy { it.tagId }.map { (tagId, list) ->
            val tag = tagMap[tagId]
            val total = list.sumOf { it.amount }
            TagStatItem(
                tagId = tagId,
                tagName = tag?.name ?: "其他",
                tagIcon = tag?.icon ?: "",
                tagColorHex = tag?.colorHex ?: 0L,
                totalAmount = total,
                count = list.size,
                showCount = tag?.showCount ?: false,
                percentage = (total / periodTotal).toFloat()
            )
        }.sortedByDescending { it.totalAmount }

        // 验证三餐：金额 55.0，次数 2，但 showCount 为 false
        val mealStat = stats.first { it.tagId == 1L }
        assertEquals(55.0, mealStat.totalAmount, 0.001)
        assertEquals(2, mealStat.count)
        assertFalse("三餐标签默认不显示消费次数", mealStat.showCount)

        // 验证谷子周边：金额 150.0，次数 1，showCount 为 true
        val goodStat = stats.first { it.tagId == 2L }
        assertEquals(150.0, goodStat.totalAmount, 0.001)
        assertEquals(1, goodStat.count)
        assertTrue("谷子标签开启了显示消费次数", goodStat.showCount)
    }

    @Test
    fun testBudgetRemainingAndProgress() {
        val monthlyBudget = 2000.0
        val monthSpent = 600.0
        val remaining = monthlyBudget - monthSpent
        val progress = (monthSpent / monthlyBudget).toFloat()

        assertEquals(1400.0, remaining, 0.001)
        assertEquals(0.3f, progress, 0.001f)
    }

    @Test
    fun testPreparedFoodMealCostCalculation() {
        val biscuit = com.example.mybudget.data.model.PreparedFoodEntity(
            id = 1,
            name = "压缩饼干",
            price = 1.8,
            unit = "块",
            category = "应急代餐"
        )
        val noodles = com.example.mybudget.data.model.PreparedFoodEntity(
            id = 2,
            name = "桶装泡面",
            price = 4.5,
            unit = "桶",
            category = "速食主食"
        )
        val sausage = com.example.mybudget.data.model.PreparedFoodEntity(
            id = 3,
            name = "火腿肠",
            price = 1.5,
            unit = "根",
            category = "佐餐加料"
        )

        val foodList = listOf(biscuit, noodles, sausage)
        // 比如这顿饭吃了：1桶泡面 + 2根火腿肠
        val selectedCounts = mapOf(2L to 1, 3L to 2)

        val mealCost = foodList.sumOf { food ->
            val count = selectedCounts[food.id] ?: 0
            food.price * count
        }
        // 4.5 * 1 + 1.5 * 2 = 7.5
        assertEquals(7.5, mealCost, 0.001)

        val canteenBaseline = 15.0
        val saved = canteenBaseline - mealCost
        assertEquals(7.5, saved, 0.001)
    }
}
