package nick.mirosh

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import nick.mirosh.repository.Result
import nick.mirosh.repository.TransactionRepo
import nick.mirosh.repository.TransactionRepoImpl
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
    val bot = Bot()
    botsApi.registerBot(bot)

    val repo: TransactionRepo = TransactionRepoImpl()
    val commandProcessor = CommandProcessor(repo)

    CoroutineScope(Dispatchers.IO).launch {
        commandProcessor.report.collect { chatIdAndReport ->
            val report = chatIdAndReport?.second
            val chatId = chatIdAndReport?.first
            val reportAsMessage = report?.joinToString("\n") ?: "No data available"
            bot.sendText(chatId, reportAsMessage)
        }
    }

    bot.onUpdateListener { update ->

        if (update.message.isCommand) {
            commandProcessor.processCommand(update.message)
            return@onUpdateListener
        }

        var transaction: Transaction
        try {
            transaction = parseUpdate(update)
        } catch (
            e: IllegalArgumentException
        ) {
            bot.sendText(update.message.chatId, "Не понимать, попробуй ещё раз \n Error: ${e.message}")
            return@onUpdateListener
        }

        runBlocking {
            val message = when (val result = repo.createTransaction(transaction)) {
                is Result.Success -> "Зописав! \n " +
                        "cумма = ${result.data.sum} \n " +
                        "категория = ${result.data.category} \n " +
                        "валюта = ${result.data.currency} \n"

                is Result.Error -> "Поняв, но не записав чет \n Error: ${result.throwable.message}"
            }
            bot.sendText(update.message.chatId, message)
        }
    }
}
