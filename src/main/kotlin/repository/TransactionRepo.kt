package nick.mirosh.repository

import com.mongodb.*
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import nick.mirosh.COLLECTION_NAME
import nick.mirosh.DATABASE_NAME
import nick.mirosh.MONGO_DB_CONNECTION_STRING
import nick.mirosh.Transaction

interface TransactionRepo {
    suspend fun createTransaction(transaction: Transaction): Result<Transaction>
    suspend fun getCurrentWeeksTransactions(): List<Transaction>
}


class TransactionRepoImpl : TransactionRepo {

    val collection = createMongoClient()

    override suspend fun createTransaction(transaction: Transaction): Result<Transaction> {
        return try {
            val result = collection.insertOne(transaction)
            println("Success! Inserted document id: " + result.insertedId)
            Result.Success(transaction)
        } catch (e: MongoException) {
            System.err.println("Unable to insert due to an error: $e")
            Result.Error(e)
        }

    }

    override suspend fun getCurrentWeeksTransactions(): List<Transaction> {
        TODO("Not yet implemented")
    }

    private fun createMongoClient(): MongoCollection<Transaction> {
        val serverApi = ServerApi.builder()
            .version(ServerApiVersion.V1)
            .build()
        val mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(MONGO_DB_CONNECTION_STRING))
            .serverApi(serverApi)
            .build()
        val client = MongoClient.create(mongoClientSettings)

        val database = client.getDatabase(DATABASE_NAME)
        return database.getCollection<Transaction>(COLLECTION_NAME)
    }
}