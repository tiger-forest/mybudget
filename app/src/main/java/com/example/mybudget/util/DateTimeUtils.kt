package com.example.mybudget.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    fun getStartAndEndOfToday(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis
        return Pair(start, end)
    }

    fun getStartAndEndOfWeek(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis
        return Pair(start, end)
    }

    fun getStartAndEndOfMonth(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        return getStartAndEndOfMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
    }

    /**
     * 获取指定年份和月份（1-12）的起止毫秒时间戳
     */
    fun getStartAndEndOfMonth(year: Int, month: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis
        return Pair(start, end)
    }

    fun getCurrentYearMonth(): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        return Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
    }

    fun getYearAndMonth(millis: Long): Pair<Int, Int> {
        val calendar = Calendar.getInstance().apply { timeInMillis = millis }
        return Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
    }

    fun formatYearMonth(year: Int, month: Int): String {
        return "${year}年${month}月"
    }

    fun getRemainingDaysInMonth(): Int {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        return (maxDays - currentDay + 1).coerceAtLeast(1)
    }

    fun formatDate(millis: Long): String {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = millis
        val targetDay = calendar.get(Calendar.DAY_OF_YEAR)
        val targetYear = calendar.get(Calendar.YEAR)

        return when {
            year == targetYear && today == targetDay -> "今天"
            year == targetYear && today - targetDay == 1 -> "昨天"
            year == targetYear -> SimpleDateFormat("M月d日 E", Locale.CHINA).format(Date(millis))
            else -> SimpleDateFormat("yyyy年M月d日", Locale.CHINA).format(Date(millis))
        }
    }

    fun formatTime(millis: Long): String {
        return SimpleDateFormat("HH:mm", Locale.CHINA).format(Date(millis))
    }

    fun formatFullDate(millis: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date(millis))
    }
}
