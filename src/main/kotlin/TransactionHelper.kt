package nick.mirosh

import org.bson.types.ObjectId
import org.telegram.telegrambots.meta.api.objects.Update

fun parseUpdate(update: Update): Transaction {

    //https://en.wikipedia.org/wiki/IETF_language_tag
    val langCode = update.message.from.languageCode

    val text = update.message.text
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
    val currencyText = parts[1]
    val category = parts.drop(2).joinToString(" ")
    val description = if (parts.size > 3) parts.drop(3).joinToString(" ") else ""

    if (sumText.isBlank()) {
        throw IllegalArgumentException(
            when (langCode) {
                "ru" -> "cумма не может быть пустой"
                else -> "Sum cannot be empty"
            }
        )
    }

    if (currencyText.isBlank()) {
        throw IllegalArgumentException("Currency cannot be empty")
    }

    if (category.isBlank()) {
        throw IllegalArgumentException("Category cannot be empty")
    }

    val sum = try {
        sumText.toDouble()
    } catch (e: NumberFormatException) {
        throw IllegalArgumentException("Invalid sum format: $sumText")
    }

    val bahtCurrencies = listOf("b", "baht", "thb", "bth", "бт", "бат", "б")
    val hryvnaCurrency = listOf("g", "grn", "uah", "грн", "гр", "г", "гривна")
    if (currencyText !in bahtCurrencies + hryvnaCurrency) {
        throw IllegalArgumentException("Invalid currency: $currencyText")
    }

    var currency = "THB"
    if (currency in hryvnaCurrency) {
        currency = "UAH"
    }
    return Transaction(ObjectId(), update.message.date, sum, currency, category, description)
}
