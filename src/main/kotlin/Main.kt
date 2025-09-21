package nick.mirosh

import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import kotlin.jvm.java
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoException
import com.mongodb.ServerApi
import com.mongodb.ServerApiVersion
import com.mongodb.kotlin.client.coroutine.MongoClient
import kotlinx.coroutines.runBlocking

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
    val bot = Bot()
    botsApi.registerBot(bot)
    val client = createMongoClient()

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

        createATransactionRecord(client, transaction) { result ->
            val message = when (result) {
                is Result.Success -> "Зописав! Правильно? \n ${result.data}"
                is Result.Error -> "Поняв, но не записав чет \n Error: ${result.throwable.message}"
            }

            bot.sendText(update.message.chatId, message)
        }
    }
}

private fun createMongoClient(): MongoClient {
    val serverApi = ServerApi.builder()
        .version(ServerApiVersion.V1)
        .build()
    val mongoClientSettings = MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(MONGO_DB_CONNECTION_STRING))
        .serverApi(serverApi)
        .build()
    return MongoClient.create(mongoClientSettings)
}

private fun createATransactionRecord(
    mongoClient: MongoClient,
    transaction: Transaction,
    onResult: (result: Result<Transaction>) -> Unit
) {
    val database = mongoClient.getDatabase(DATABASE_NAME)
    val collection = database.getCollection<Transaction>(COLLECTION_NAME)
    runBlocking {
        try {
            val result = collection.insertOne(transaction)
            println("Success! Inserted document id: " + result.insertedId)
            onResult(Result.Success(transaction))
        } catch (e: MongoException) {
            System.err.println("Unable to insert due to an error: $e")
            onResult(Result.Error(e))
        }
        mongoClient.close()
    }
}