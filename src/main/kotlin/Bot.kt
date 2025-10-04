package nick.mirosh

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.Update

class Bot(
    private val commandManager: CommandManager,
    private val transactionManager: TransactionManager
) : TelegramLongPollingBot() {

    init {
        listenForCommandResults()
    }

    override fun getBotUsername(): String? {
        return "Финансовый помощник Асеньки и Коленьки"
    }

    override fun getBotToken() = BOT_TOKEN

    override fun onUpdateReceived(update: Update) {
        println(update)
        val message = update.message
        //TODO do I even need these checks?
        if (message.isGroupMessage || message.isSuperGroupMessage || message.isUserMessage) {
            if (message.isCommand) {
                commandManager.processCommand(message)
            } else {
                transactionManager.processTransaction(update)
            }
        }
    }

    private fun listenForCommandResults() {
        CoroutineScope(Dispatchers.IO).launch {
            commandManager.report.collect { chatIdAndReport ->
                chatIdAndReport?.let {
                    val report = chatIdAndReport.second
                    val chatId = chatIdAndReport.first
                    val reportAsMessage = report.joinToString("\n")
                    sendText(chatId, reportAsMessage)
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            transactionManager.message.collect { chatIdAndMessage ->
                chatIdAndMessage?.let {
                    sendText(it.first, it.second)
                }
            }
        }
    }

    fun sendText(who: Long?, what: String) {
        val sm = SendMessage.builder().chatId(who.toString()).text(what).build()
        try {
            execute(sm)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}