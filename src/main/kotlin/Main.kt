package nick.mirosh

import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import kotlin.jvm.java

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {

    val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
    val bot = Bot()
    botsApi.registerBot(bot)
}