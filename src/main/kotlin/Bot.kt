package nick.mirosh

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

class Bot : TelegramLongPollingBot() {

    override fun getBotUsername(): String? {
        return "Финансовый помощник Асеньки и Коленьки"
    }

    private var updateListener: ((Update) -> Unit)? = null

    fun onUpdateListener(listener: (Update) -> Unit) {
        this.updateListener = listener
    }


    override fun getBotToken() = BOT_TOKEN

    override fun onUpdateReceived(update: Update) {
        val message = update.message
        if (message.isGroupMessage || message.isSuperGroupMessage) {
            updateListener?.invoke(update)
        }
//        showMenuOptions(update)

    }


    private fun showMenuOptions(update: Update) {
         when {
            update.hasMessage() && update.message.hasText() &&
                    update.message.text == "/set_default_currency" -> {
                sendOptions(update.message.chatId)
            }

            update.hasCallbackQuery() -> {
                val cq = update.callbackQuery
                val choice = when (cq.data) {
                    "opt_A" -> "Option A"
                    "opt_B" -> "Option B"
                    else -> "Unknown"
                }
                println("choice: $choice")

                // A. Edit the original message to show selection:
//                execute(
//                    EditMessageText(
//                        cq.message.chatId.toString(),
//                        cq.message.messageId,
//                        "You chose: $choice"
//                    )
//                )

                // (Or B. send a new message instead of editing)
                // execute(SendMessage(cq.message.chatId.toString(), "You chose: $choice"))
            }
        }
    }


    fun showMenu(who: Long?) {
        val sm = SendMessage.builder().chatId(who.toString())
            .text("Привет, я твой финансовый помощник").build()
        try {
            execute(sm)
        } catch (e: Exception) {
            e.printStackTrace()
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


    //https://stackoverflow.com/questions/32423837/telegram-bot-how-to-get-a-group-chat-id

    //https://core.telegram.org/bots/tutorial#introduction
    private fun sendOptions(chatId: Long) {
        val buttons = listOf(
            listOf(InlineKeyboardButton("Option A").apply { callbackData = "opt_A" }),
            listOf(InlineKeyboardButton("Option B").apply { callbackData = "opt_B" })
        )
        val markup = InlineKeyboardMarkup(buttons)

        val msg = SendMessage(chatId.toString(), "Please send me a 3 character currency code you want to be a default one")
        msg.replyMarkup = markup
        execute(msg)
    }
}