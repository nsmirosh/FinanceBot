package nick.mirosh

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nick.mirosh.utils.Category
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

class Bot(
    private val commandManager: CommandManager,
    private val transactionManager: TransactionManager,
) : TelegramLongPollingBot() {

    init {
        listenForCommandResults()
    }

    override fun getBotUsername(): String? {
        return "Финансовый помощник Асеньки и Коленьки"
    }

    override fun getBotToken() = BOT_TOKEN

    override fun onUpdateReceived(update: Update) {
        val message = update.message
        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update)
            return
        }
        if (message.isGroupMessage || message.isSuperGroupMessage || message.isUserMessage) {
            if (message.isCommand) {
                commandManager.processCommand(message)
            } else {
                transactionManager.processTransaction(update)
            }
        }
    }

    private fun handleCallbackQuery(update: Update) {
        val callbackQuery = update.callbackQuery
        val chatId = callbackQuery.message.chatId.toString()
        val data = callbackQuery.data

        if (data.startsWith("category:")) {
            val categoryText = data.substringAfter("category:")
            val category = Category.valueOf(categoryText)
            sendText(chatId.toLong(), "Building a report for ${category.displayName}. Please wait...")
            try {
                commandManager.sendWeeklyReport(update.callbackQuery.message.chatId, category)
            } catch (e: Exception) {
                sendText(chatId.toLong(), "Error building report: ${e.message}")
            }
        }
    }

    fun buildCategoryKeyboard(who: Long) {
        val userCategories = Category.entries

        val rows = userCategories.map { category ->
            listOf(InlineKeyboardButton().apply {
                text = category.name
                callbackData = "category:${category.name}"
            })
        }
        val markup = InlineKeyboardMarkup()
        markup.keyboard = rows

        val sm = SendMessage
            .builder()
            .chatId(who.toString())
            .replyMarkup(markup)
            .text("choose a category you want a report for")
            .build()
        try {
            execute(sm)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun listenForCommandResults() {
        CoroutineScope(Dispatchers.IO).launch {
            transactionManager.message.collect { chatIdAndMessage ->
                chatIdAndMessage?.let {
                    sendText(it.first, it.second)
                }
            }
        }
        CoroutineScope(Dispatchers.Default).launch {
            commandManager.showKeyboard.collect {
                buildCategoryKeyboard(it)
            }
        }
        CoroutineScope(Dispatchers.Default).launch {
            commandManager.showError.collect {
                sendText(it.first, it.second)
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
