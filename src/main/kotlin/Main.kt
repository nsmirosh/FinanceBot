package nick.mirosh

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

    bot.onUpdateListener { update ->
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
                is Result.Success -> "Зописав! Правильно? \n ${result.data}"
                is Result.Error -> "Поняв, но не записав чет \n Error: ${result.throwable.message}"
            }
            bot.sendText(update.message.chatId, message)
        }
    }
}
