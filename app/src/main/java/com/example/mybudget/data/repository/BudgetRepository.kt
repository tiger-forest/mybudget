package com.example.mybudget.data.repository

import com.example.mybudget.data.dao.BudgetDao
import com.example.mybudget.data.dao.ExpenseDao
import com.example.mybudget.data.dao.PreparedFoodDao
import com.example.mybudget.data.dao.TagDao
import com.example.mybudget.data.model.BudgetPreference
import com.example.mybudget.data.model.ExpenseEntity
import com.example.mybudget.data.model.MonthlyBudgetEntity
import com.example.mybudget.data.model.PreparedFoodEntity
import com.example.mybudget.data.model.TagEntity
import kotlinx.coroutines.flow.Flow

class BudgetRepository(
    private val tagDao: TagDao,
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao,
    private val preparedFoodDao: PreparedFoodDao
) {
    val allTags: Flow<List<TagEntity>> = tagDao.getAllTags()
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val budget: Flow<BudgetPreference?> = budgetDao.getBudget()
    val allMonthlyBudgets: Flow<List<MonthlyBudgetEntity>> = budgetDao.getAllMonthlyBudgets()
    val allPreparedFoods: Flow<List<PreparedFoodEntity>> = preparedFoodDao.getAllPreparedFoods()

    fun getExpensesBetween(startTime: Long, endTime: Long): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesBetween(startTime, endTime)
    }

    suspend fun insertExpense(expense: ExpenseEntity): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteExpenseById(id: Long) {
        expenseDao.deleteExpenseById(id)
    }

    suspend fun insertTag(tag: TagEntity): Long {
        return tagDao.insertTag(tag)
    }

    suspend fun updateTag(tag: TagEntity) {
        tagDao.updateTag(tag)
        expenseDao.updateExpensesTagInfo(tag.id, tag.name, tag.icon, tag.colorHex)
    }

    suspend fun deleteTag(tag: TagEntity) {
        tagDao.deleteTag(tag)
    }

    suspend fun updateTagShowCount(tagId: Long, showCount: Boolean) {
        tagDao.updateShowCount(tagId, showCount)
    }

    suspend fun setBudget(amount: Double) {
        budgetDao.setBudget(BudgetPreference(id = 1, monthlyBudget = amount))
    }

    fun getMonthlyBudget(year: Int, month: Int): Flow<MonthlyBudgetEntity?> {
        return budgetDao.getMonthlyBudget(year, month)
    }

    suspend fun setMonthlyBudget(year: Int, month: Int, amount: Double) {
        budgetDao.setMonthlyBudget(MonthlyBudgetEntity(year = year, month = month, budget = amount))
    }

    suspend fun deleteMonthlyBudget(year: Int, month: Int) {
        budgetDao.deleteMonthlyBudget(year, month)
    }

    suspend fun insertPreparedFood(food: PreparedFoodEntity): Long {
        return preparedFoodDao.insertFood(food)
    }

    suspend fun updatePreparedFood(food: PreparedFoodEntity) {
        preparedFoodDao.updateFood(food)
    }

    suspend fun deletePreparedFood(food: PreparedFoodEntity) {
        preparedFoodDao.deleteFood(food)
    }

    suspend fun deletePreparedFoodById(id: Long) {
        preparedFoodDao.deleteFoodById(id)
    }
}
