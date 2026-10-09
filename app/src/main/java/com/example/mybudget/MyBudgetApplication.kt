package com.example.mybudget

import android.app.Application
import com.example.mybudget.data.db.AppDatabase
import com.example.mybudget.data.repository.BudgetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MyBudgetApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy {
        BudgetRepository(
            tagDao = database.tagDao(),
            expenseDao = database.expenseDao(),
            budgetDao = database.budgetDao()
        )
    }
}
