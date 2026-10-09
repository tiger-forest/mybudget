package com.example.mybudget.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mybudget.data.dao.BudgetDao
import com.example.mybudget.data.dao.ExpenseDao
import com.example.mybudget.data.dao.TagDao
import com.example.mybudget.data.model.BudgetPreference
import com.example.mybudget.data.model.ExpenseEntity
import com.example.mybudget.data.model.MonthlyBudgetEntity
import com.example.mybudget.data.model.TagEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `monthly_budgets` (
                `year` INTEGER NOT NULL,
                `month` INTEGER NOT NULL,
                `budget` REAL NOT NULL,
                PRIMARY KEY(`year`, `month`)
            )
            """.trimIndent()
        )
    }
}

@Database(
    entities = [TagEntity::class, ExpenseEntity::class, BudgetPreference::class, MonthlyBudgetEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tagDao(): TagDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_budget_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.tagDao(), database.budgetDao())
                    }
                }
            }

            private suspend fun populateInitialData(tagDao: TagDao, budgetDao: BudgetDao) {
                // 初始化大学生活费默认预算：2000元
                budgetDao.setBudget(BudgetPreference(id = 1, monthlyBudget = 2000.0))

                // 预置符合大学生生活场景的标签集合
                // 注意：showCount 默认大多为 false，用户可以在标签管理页一键开启
                val initialTags = listOf(
                    TagEntity(name = "三餐", icon = "🍜", colorHex = 0xFFFF7043, showCount = false, isDefault = true),
                    TagEntity(name = "饮料奶茶", icon = "🧋", colorHex = 0xFFFFB74D, showCount = false, isDefault = true),
                    TagEntity(name = "谷子周边", icon = "🎁", colorHex = 0xFFAB47BC, showCount = false, isDefault = true),
                    TagEntity(name = "学习备考", icon = "📚", colorHex = 0xFF42A5F5, showCount = false, isDefault = true),
                    TagEntity(name = "生活日用", icon = "🧻", colorHex = 0xFF26A69A, showCount = false, isDefault = true),
                    TagEntity(name = "娱乐游戏", icon = "🎮", colorHex = 0xFFEC407A, showCount = false, isDefault = true),
                    TagEntity(name = "出行交通", icon = "🚌", colorHex = 0xFF78909C, showCount = false, isDefault = true),
                    TagEntity(name = "聚会社交", icon = "🍻", colorHex = 0xFFFFA726, showCount = false, isDefault = true)
                )
                tagDao.insertTags(initialTags)
            }
        }
    }
}
