package nick.mirosh.repository

import com.mongodb.*
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.toList
import nick.mirosh.DATABASE_NAME
import nick.mirosh.MONGO_DB_CONNECTION_STRING
import nick.mirosh.Transaction
import utils.weekInCurrentMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

data class Budgets(
    val values: List<Pair<String, Int>>
)

data class Budget(
    val category: String,
    val amountForMonth: Int,
    val amountForCurrentWeek: Int
)


interface TransactionRepo {
    suspend fun createTransaction(transaction: Transaction): Result<Transaction>
    suspend fun getCurrentWeekTransactions(): List<Transaction>
    suspend fun setHardCodedBudgets()
    suspend fun setBudget(budget: Budget): Result<Unit>
    suspend fun getBudgets(): List<Budget>

}


const val TRANSACTIONS_COLLECTION_NAME = "transactions"
const val BUDGETS_COLLECTION_NAME = "budgets"

class TransactionRepoImpl : TransactionRepo {

    val database = createMongoClient()
    val transactions = database.getCollection<Transaction>(TRANSACTIONS_COLLECTION_NAME)
    val budgets = database.getCollection<Budget>(BUDGETS_COLLECTION_NAME)

    override suspend fun createTransaction(transaction: Transaction): Result<Transaction> {
        return try {
            val result = transactions.insertOne(transaction)
            println("Success! Inserted document id: " + result.insertedId)
            Result.Success(transaction)
        } catch (e: MongoException) {
            System.err.println("Unable to insert due to an error: $e")
            Result.Error(e)
        }

    }

    override suspend fun getCurrentWeekTransactions(): List<Transaction> {

        //Bangkok is 7 hours ahead of UTC - hardcode it for now
        val zone = ZoneId.of("Asia/Bangkok")
        val today = LocalDate.now(zone)

        // Start of week (Monday 00:00)
        val startOfWeekZdt = today
            .with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDay(zone)
        println("startOfWeekZdt: $startOfWeekZdt")

        // End of week (exclusive) = next Monday 00:00
        val endOfWeekExclusiveZdt = startOfWeekZdt.plusDays(7)

        // Convert both to UTC
        val startUtc = startOfWeekZdt.withZoneSameInstant(ZoneOffset.UTC)
        val endUtc = endOfWeekExclusiveZdt.withZoneSameInstant(ZoneOffset.UTC)

        val startMillis = startUtc.toInstant().toEpochMilli() / 1000
        val endMillisExclusive = endUtc.toInstant().toEpochMilli() / 1000
        println("Start of week: $startMillis, end of week: $endMillisExclusive")

        val filter = Filters.and(
            Filters.gte("utcDate", startMillis),
            Filters.lt("utcDate", endMillisExclusive)
        )

        return transactions.find(filter).toList()
    }

    private fun createMongoClient(): MongoDatabase {
        val serverApi = ServerApi.builder()
            .version(ServerApiVersion.V1)
            .build()
        val mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(MONGO_DB_CONNECTION_STRING))
            .serverApi(serverApi)
            .build()
        val client = MongoClient.create(mongoClientSettings)

        return client.getDatabase(DATABASE_NAME)

//        return database.getCollection<Transaction>(COLLECTION_NAME)
    }


    override suspend fun setHardCodedBudgets() {

        val weeksInCurrentMonth = weekInCurrentMonth()
        println("Weeks in current month: $weeksInCurrentMonth")

        val groceriesBudget = 2900000
        val restaurantsBudget = 645000
        val coffeeBudget = 320000

        val budgets = listOf(
            Budget("Groceries", groceriesBudget, (groceriesBudget / weeksInCurrentMonth).toInt()),
            Budget("Entertainment", 0, 0),
            Budget("Restaurants", restaurantsBudget, (restaurantsBudget / weeksInCurrentMonth).toInt()),
            Budget("Coffee", coffeeBudget, (coffeeBudget / weeksInCurrentMonth).toInt())
        )
        try {
            val result = this@TransactionRepoImpl.budgets.insertMany(budgets)
            println("Success! Inserted document id: " + result.insertedIds)
//            Result.Success(transaction)
        } catch (e: MongoException) {
            System.err.println("Unable to insert due to an error: $e")
//            Result.Error(e)
        }
    }

    override suspend fun setBudget(budget: Budget): Result<Unit> =
        try {
            val result = this@TransactionRepoImpl.budgets.insertOne(budget)
            println("Success! Inserted document id: " + result.insertedId)
            Result.Success(Unit)
        } catch (e: MongoException) {
            System.err.println("Unable to insert due to an error: $e")
            Result.Error(e)
        }


    override suspend fun getBudgets(): List<Budget> {
        return budgets.find().toList()
    }
}