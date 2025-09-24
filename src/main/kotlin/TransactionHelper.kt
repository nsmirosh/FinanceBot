package nick.mirosh

import org.bson.types.ObjectId
import org.telegram.telegrambots.meta.api.objects.Update

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
    val description = if (parts.size > 3) parts.drop(3).toString() else ""

    if (secondArg.isBlank()) {
        throw IllegalArgumentException("Currency cannot be empty")
    }

    val sum = try {
        sumText.toDouble()
    } catch (e: NumberFormatException) {
        throw IllegalArgumentException("Invalid sum format: $sumText")
    }

    val currency = determineCurrency(secondArg)
    val category = determineCategory(categoryText)

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


private fun determineCategory(categoryText: String): String {
    val categoryText = categoryText.lowercase()

    val coffeeKeywords = listOf("кофе", "coffee", "cofe", "cafe", "кофа", "кава", "коф")
    val groceryKeywords = listOf(
        "продукты",
        "grocery",
        "groceries",
        "food",
        "supermarket",
        "market",
        "shop",
        "store",
        "products",
        "продукт",
        "супермаркет",
        "магазин",
        "магаз",
        "Японский",
        "еда",
        "закупка",
        "покупки",
        "прод",
        "харчи",
        "хавка"
    )

    val restaurantKeywords = listOf(
        "рестораны",
        "restaurant",
        "resto",
        "rest",
        "dining",
        "dinner",
        "lunch",
        "eatery",
        "bistro",
        "fastfood",
        "рест",
        "ресторан",
        "кафешка",
        "кафурик",
        "закусочная",
        "забегаловка",
        "столовка",
        "фастфуд"
    )
    val entertainmentKeywords = listOf(
        "развлечения",
        "entertainment",
        "movie",
        "cinema",
        "film",
        "theater",
        "theatre",
        "concert",
        "show",
        "club",
        "party",
        "fun",
        "games",
        "gaming",
        "event",
        "festival",
        "кино"
    )
    return when (categoryText) {
        in coffeeKeywords -> "coffee"
        in groceryKeywords -> "grocery"
        in restaurantKeywords -> "restaurant"
        in entertainmentKeywords -> "entertainment"
        else -> "other"
    }
}
