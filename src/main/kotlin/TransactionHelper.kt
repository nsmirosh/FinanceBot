package nick.mirosh

import org.bson.types.ObjectId
import org.telegram.telegrambots.meta.api.objects.Update

fun parseUpdate(update: Update): Transaction {

    val text = update.message.text
    val parts = text.trim().split(" ")

    if (parts.size < 3) {
        throw IllegalArgumentException("Invalid format. Expected format: 'amount currency category'")
    }

    val sumText = parts[0]
    val currencyText = parts[1]
    val category = parts.drop(2).joinToString(" ")

    if (sumText.isBlank()) {
        throw IllegalArgumentException("Sum cannot be empty")
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
    return Transaction(ObjectId(), update.message.date, sum, currency, category)
}
