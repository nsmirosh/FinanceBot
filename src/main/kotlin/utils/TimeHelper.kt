package utils

import java.time.LocalDate
import java.time.ZoneId

fun weekInCurrentMonth(): Float {
    val zone = ZoneId.of("Asia/Bangkok")
    val today = LocalDate.now(zone)
    return today.lengthOfMonth() / 7.0f
}
