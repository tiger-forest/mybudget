package com.example.mybudget.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mybudget.data.model.BudgetPreference
import com.example.mybudget.data.model.ExpenseEntity
import com.example.mybudget.data.model.MonthlyBudgetEntity
import com.example.mybudget.data.model.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY id ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<TagEntity>)

    @Update
    suspend fun updateTag(tag: TagEntity)

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Query("UPDATE tags SET showCount = :showCount WHERE id = :tagId")
    suspend fun updateShowCount(tagId: Long, showCount: Boolean)

    @Query("SELECT COUNT(*) FROM tags")
    suspend fun getTagCount(): Int
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE dateMillis BETWEEN :startTime AND :endTime ORDER BY dateMillis DESC")
    fun getExpensesBetween(startTime: Long, endTime: Long): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("UPDATE expenses SET tagName = :name, tagIcon = :icon, tagColorHex = :colorHex WHERE tagId = :tagId")
    suspend fun updateExpensesTagInfo(tagId: Long, name: String, icon: String, colorHex: Long)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget_preference WHERE id = 1 LIMIT 1")
    fun getBudget(): Flow<BudgetPreference?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBudget(budget: BudgetPreference)

    @Query("SELECT * FROM monthly_budgets WHERE year = :year AND month = :month LIMIT 1")
    fun getMonthlyBudget(year: Int, month: Int): Flow<MonthlyBudgetEntity?>

    @Query("SELECT * FROM monthly_budgets ORDER BY year DESC, month DESC")
    fun getAllMonthlyBudgets(): Flow<List<MonthlyBudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setMonthlyBudget(monthlyBudget: MonthlyBudgetEntity)

    @Query("DELETE FROM monthly_budgets WHERE year = :year AND month = :month")
    suspend fun deleteMonthlyBudget(year: Int, month: Int)
}
