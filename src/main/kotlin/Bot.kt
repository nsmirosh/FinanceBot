package nick.mirosh

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

class Bot : TelegramLongPollingBot() {

    override fun getBotUsername(): String? {
        return "Финансовый помощник Асеньки и Коленьки"
    }

    override fun getBotToken() = BOT_TOKEN

    override fun onUpdateReceived(update: Update) {
        println("updateReceived = $update")
        val message = update.message

        var transaction: Transaction? = null

        if (message.isGroupMessage || message.isSuperGroupMessage) {
            try {
                transaction = parseText(message.text)
            } catch (
                e: IllegalArgumentException
            ) {
                sendText(update.message.chatId, "Something went wrong : ${e.message}")
            }
        }

        sendText(
            update.message.chatId,
            "Правильно ли я понял мой господин?  \n $transaction"
        )
    }


    private fun sumUpForTheDay(update: Update) {

    }

    data class Transaction(
        val sum: Double,
        val currency: String,
        val category: String
    )


    fun parseText(text: String): Transaction {
        val parts = text.trim().split(" ")

        if (parts.size < 3) {
            throw IllegalArgumentException("Invalid format. Expected format: 'amount currency category'")
        }

        val sumText = parts[0]
        val currency = parts[1]
        val category = parts.drop(2).joinToString(" ")

        if (sumText.isBlank()) {
            throw IllegalArgumentException("Sum cannot be empty")
        }

        if (currency.isBlank()) {
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

        val bahtCurrencies = listOf("b", "baht", "bth", "бат", "б")
        val hryvnaCurrency = listOf("g", "grn", "uah", "грн", "г", "гривна")
        if (currency !in bahtCurrencies + hryvnaCurrency) {
            throw IllegalArgumentException("Invalid currency: $currency")
        }
        if (currency in bahtCurrencies) {
            return Transaction(sum, "baht", category)
        }
        return Transaction(sum, "hryvna", category)
    }



    fun sendText(who: Long?, what: String) {
        val sm = SendMessage.builder().chatId(who.toString()).text(what).build()
        try {
            execute(sm)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    //https://stackoverflow.com/questions/32423837/telegram-bot-how-to-get-a-group-chat-id

    //https://core.telegram.org/bots/tutorial#introduction
}