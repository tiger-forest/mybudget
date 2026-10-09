package com.example.mybudget.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mybudget.data.dao.BudgetDao
import com.example.mybudget.data.dao.ExpenseDao
import com.example.mybudget.data.dao.PreparedFoodDao
import com.example.mybudget.data.dao.TagDao
import com.example.mybudget.data.model.BudgetPreference
import com.example.mybudget.data.model.ExpenseEntity
import com.example.mybudget.data.model.MonthlyBudgetEntity
import com.example.mybudget.data.model.PreparedFoodEntity
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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `prepared_foods` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `price` REAL NOT NULL,
                `unit` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `note` TEXT NOT NULL
            )
            """.trimIndent()
        )
        // 预置大学生常见自备囤货食品
        db.execSQL("INSERT INTO `prepared_foods` (`name`, `price`, `unit`, `category`, `note`) VALUES ('袋装方便面', 2.5, '包', '速食主食', '普通水煮/干拌')")
        db.execSQL("INSERT INTO `prepared_foods` (`name`, `price`, `unit`, `category`, `note`) VALUES ('桶装泡面', 4.5, '桶', '速食主食', '免洗碗快捷')")
        db.execSQL("INSERT INTO `prepared_foods` (`name`, `price`, `unit`, `category`, `note`) VALUES ('压缩饼干', 1.8, '块', '应急代餐', '抗饿耐储存')")
        db.execSQL("INSERT INTO `prepared_foods` (`name`, `price`, `unit`, `category`, `note`) VALUES ('火腿肠', 1.5, '根', '佐餐加料', '泡面经典伴侣')")
        db.execSQL("INSERT INTO `prepared_foods` (`name`, `price`, `unit`, `category`, `note`) VALUES ('卤蛋', 1.5, '个', '佐餐加料', '补充蛋白质')")
        db.execSQL("INSERT INTO `prepared_foods` (`name`, `price`, `unit`, `category`, `note`) VALUES ('自热米饭', 12.0, '盒', '速食主食', '荤素搭配自热餐')")
        db.execSQL("INSERT INTO `prepared_foods` (`name`, `price`, `unit`, `category`, `note`) VALUES ('螺蛳粉', 9.9, '袋', '速食主食', '风味浓郁')")
        db.execSQL("INSERT INTO `prepared_foods` (`name`, `price`, `unit`, `category`, `note`) VALUES ('纯牛奶', 3.0, '盒', '冲饮零食', '早晚加餐')")
    }
}

@Database(
    entities = [TagEntity::class, ExpenseEntity::class, BudgetPreference::class, MonthlyBudgetEntity::class, PreparedFoodEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tagDao(): TagDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun preparedFoodDao(): PreparedFoodDao

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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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
                        populateInitialData(database.tagDao(), database.budgetDao(), database.preparedFoodDao())
                    }
                }
            }

            private suspend fun populateInitialData(tagDao: TagDao, budgetDao: BudgetDao, foodDao: PreparedFoodDao) {
                // 初始化大学生活费默认预算：2000元
                budgetDao.setBudget(BudgetPreference(id = 1, monthlyBudget = 2000.0))

                // 预置符合大学生生活场景的标签集合
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

                // 预置大学生常见自备囤货食品
                val initialFoods = listOf(
                    PreparedFoodEntity(name = "袋装方便面", price = 2.5, unit = "包", category = "速食主食", note = "普通水煮/干拌"),
                    PreparedFoodEntity(name = "桶装泡面", price = 4.5, unit = "桶", category = "速食主食", note = "免洗碗快捷"),
                    PreparedFoodEntity(name = "压缩饼干", price = 1.8, unit = "块", category = "应急代餐", note = "抗饿耐储存"),
                    PreparedFoodEntity(name = "火腿肠", price = 1.5, unit = "根", category = "佐餐加料", note = "泡面经典伴侣"),
                    PreparedFoodEntity(name = "卤蛋", price = 1.5, unit = "个", category = "佐餐加料", note = "补充蛋白质"),
                    PreparedFoodEntity(name = "自热米饭", price = 12.0, unit = "盒", category = "速食主食", note = "荤素搭配自热餐"),
                    PreparedFoodEntity(name = "螺蛳粉", price = 9.9, unit = "袋", category = "速食主食", note = "风味浓郁"),
                    PreparedFoodEntity(name = "纯牛奶", price = 3.0, unit = "盒", category = "冲饮零食", note = "早晚加餐")
                )
                foodDao.insertFoods(initialFoods)
            }
        }
    }
}

