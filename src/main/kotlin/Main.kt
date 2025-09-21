package nick.mirosh

import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import kotlin.jvm.java
import org.bson.Document
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
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
    connectToDatabase()
}


private fun connectToDatabase() {
    val serverApi = ServerApi.builder()
        .version(ServerApiVersion.V1)
        .build()
    val mongoClientSettings = MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(MONGO_DB_CONNECTION_STRING))
        .serverApi(serverApi)
        .build()
    // Create a new client and connect to the server
    MongoClient.create(mongoClientSettings).use { mongoClient ->
        val database = mongoClient.getDatabase("admin")
        runBlocking {
            database.runCommand(Document("ping", 1))
        }
        println("Pinged your deployment. You successfully connected to MongoDB!")
    }
}