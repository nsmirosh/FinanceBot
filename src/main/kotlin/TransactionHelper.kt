package nick.mirosh

import nick.mirosh.utils.Category
import org.bson.types.ObjectId
import org.telegram.telegrambots.meta.api.objects.Update
import nick.mirosh.utils.Category.*
import nick.mirosh.utils.TriggerKeyWords
import nick.mirosh.utils.parseSumText

fun parseUpdate(update: Update): Transaction {

    //https://en.wikipedia.org/wiki/IETF_language_tag

    val name = update.message.from.firstName
    val message = update.message
    val langCode = message.from.languageCode

    val text =
        message.text ?: message.caption ?: throw IllegalArgumentException("No text or caption found")
    val parts = text.trim().split(" ")

    if (parts.size < 3) {
        throw IllegalArgumentException(
            when (langCode) {
                "ru" -> "Неправильный формат. Ожидаемый формат: 'сумма, валюта, категория, описание(опционально)'  '"
                else -> "Invalid format. Expected format: 'amount currency category'"
            }
        )
    }

    val sumText = parts[0]
    val secondArg = parts[1]
    val categoryText = parts[2]
    var description = if (parts.size > 3) parts.drop(3).toString() else ""

    if (secondArg.isBlank()) {
        throw IllegalArgumentException("Currency cannot be empty")
    }
    val sum = try {
        parseSumText(sumText)
    } catch (e: Exception) {
        throw IllegalArgumentException("Invalid sum format: $sumText")
    }

    val currency = determineCurrency(secondArg)
    val category = determineCategory(categoryText)
    if (category == OTHER && parts.size == 3) {
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
