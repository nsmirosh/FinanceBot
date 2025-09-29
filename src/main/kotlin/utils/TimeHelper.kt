package utils

import java.time.LocalDate
import java.time.ZoneId

fun weekInCurrentMonth(): Float {
        val zone = ZoneId.of("Asia/Bangkok")
        val today = LocalDate.now(zone)
        val lastOfMonth = today.withDayOfMonth(today.lengthOfMonth())

        val totalDaysInMonth = lastOfMonth.dayOfMonth
        return totalDaysInMonth / 7.0f
    }
