package nick.mirosh

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

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
}