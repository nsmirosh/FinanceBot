package nick.mirosh.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import nick.mirosh.Bot
import nick.mirosh.CommandManager
import nick.mirosh.TransactionManager
import nick.mirosh.networking.TelegramApiManager
import nick.mirosh.repository.TransactionRepo
import nick.mirosh.repository.TransactionRepoImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.generics.LongPollingBot
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

val appModule = module {

    factory { TransactionRepoImpl() } bind TransactionRepo::class
    factoryOf(::TransactionManager)
    factoryOf(::CommandManager)

    single { Bot(get(), get()) } bind LongPollingBot::class

    single<TelegramBotsApi>(createdAtStart = true) {
        val api = TelegramBotsApi(DefaultBotSession::class.java)
        api.registerBot(get())
        api
    }

    single {
        HttpClient(Java) {
            install(ContentNegotiation) {
                json()
            }
            install(Logging) {
                level = LogLevel.ALL
            }
        }
    }
    singleOf(::TelegramApiManager)
}
