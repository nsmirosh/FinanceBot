package nick.mirosh

import nick.mirosh.utils.Category
import org.bson.types.ObjectId
import org.telegram.telegrambots.meta.api.objects.Update
import nick.mirosh.utils.Category.*
import nick.mirosh.utils.TriggerKeyWords
import nick.mirosh.utils.parseSumText
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

fun parseUpdate(update: Update): Transaction {

    //https://en.wikipedia.org/wiki/IETF_language_tag

    val name = update.message.from.firstName
    val message = update.message
    val langCode = message.from.languageCode

    val text =
        message.text ?: message.caption ?: throw IllegalArgumentException("No text or caption found")
    val parts = text.trim().split(" ")

    if (parts.size < 2) {
        throw IllegalArgumentException(
            when (langCode) {
//                "ru" -> "Неправильный формат. Ожидаемый формат: 'сумма, валюта, категория, описание(опционально)'  '"
                "ru" -> "Неправильный формат. Ожидаемый формат: 'сумма, категория, описание(опционально)'  '"
                else -> "Invalid format. Expected format: 'amount category description(optional)' '"
            }
        )
    }

    val sumText = parts[0]
//    val secondArg = parts[1]
    val categoryText = parts[1]

    var description = if (parts.size > 2) parts.drop(2).toString() else ""

//    if (secondArg.isBlank()) {
//        throw IllegalArgumentException("Currency cannot be empty")
//    }
    val sum = try {
        parseSumText(sumText)
    } catch (e: Exception) {
        throw IllegalArgumentException("Invalid sum format: $sumText")
    }

//    val currency = determineCurrency(secondArg)
    val currency = "THB"
    val category = determineCategory(categoryText)
    if (category == OTHER || description.isEmpty()) {
       description = categoryText
    }

    return Transaction(ObjectId(), update.message.date, sum, currency, category, description, name)
}



private fun determineCurrency(currencyText: String): String {

    val bahtCurrencies = listOf("b", "baht", "thb", "bth", "бт", "бат", "б")
    val hryvnaCurrency = listOf("g", "grn", "uah", "грн", "гр", "г", "гривна")
    if (currencyText !in bahtCurrencies + hryvnaCurrency) {
        throw IllegalArgumentException("Invalid currency: $currencyText")
    }

    var currency = "THB"
    if (currency in hryvnaCurrency) {
        currency = "UAH"
    }
    return currency
}


private fun determineCategory(categoryText: String): Category {
    val categoryText = categoryText.lowercase()

    with(TriggerKeyWords) {
        return when (categoryText) {
            in coffeeKeywords -> COFFEE
            in groceryKeywords -> GROCERIES
            in restaurantKeywords -> RESTAURANTS
            in entertainmentKeywords -> ENTERTAINMENT
            in utilities -> UTILITIES
            in healthKeywords -> HEALTH
            in subscriptionKeywords -> SUBSCRIPTIONS
            in houseKeywords -> HOUSE
            in clothesKeywords -> CLOTHES
            in transportKeywords -> TRANSPORT
            in educationKeywords -> EDUCATION
            else -> OTHER
        }
    }
}

fun calculateThisWeekBudget(
    monthBudget: Int,
    zone: ZoneId = ZoneId.of("Asia/Bangkok")
): Int {
    // Ensure we evaluate based on provided date (zone param kept for clarity / future extension)

    val currentDate = LocalDate.now(zone)

    val dailyBudget = monthBudget / currentDate.lengthOfMonth()

    // Find Monday of the current week (previous or same Monday)
    val monday = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val sunday = monday.plusDays(6)

    return dailyBudget * when {
        // Sunday is in a different month -> week goes into next month (partial at end of month)
        sunday.month != currentDate.month -> {
            // Days left in current month including today
            currentDate.lengthOfMonth() - currentDate.dayOfMonth + 1
        }

        // Monday is in a different month -> week started in previous month (partial at start of month)
        monday.month != currentDate.month -> {
            // Days from start of current month up to and including today
            currentDate.dayOfMonth
        }

        // Full week inside the same month
        else -> 7
    }
}
