package nick.mirosh.repository

import com.mongodb.*
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.toList
import nick.mirosh.DATABASE_NAME
import nick.mirosh.MONGO_DB_CONNECTION_STRING
import nick.mirosh.Transaction
import nick.mirosh.utils.Category
import utils.weekInCurrentMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import nick.mirosh.utils.Category.*

data class Budget(
    val category: Category,
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


    override suspend fun createTransaction(transaction: Transaction): Result<Transaction> {
        return try {
            val result = transactions.insertOne(transaction)
            Result.Success(transaction)
        } catch (e: MongoException) {
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

        // End of week (exclusive) = next Monday 00:00
        val endOfWeekExclusiveZdt = startOfWeekZdt.plusDays(7)

        // Convert both to UTC
        val startUtc = startOfWeekZdt.withZoneSameInstant(ZoneOffset.UTC)
        val endUtc = endOfWeekExclusiveZdt.withZoneSameInstant(ZoneOffset.UTC)

        val startMillis = startUtc.toInstant().toEpochMilli() / 1000
        val endMillisExclusive = endUtc.toInstant().toEpochMilli() / 1000

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

    }


    override suspend fun setHardCodedBudgets() {

        val weeksInCurrentMonth = weekInCurrentMonth()

        //In thai baht cents
        val groceriesBudget = 2900000
        val restaurantsBudget = 645000
        val coffeeBudget = 320000
        val education = 620000
        val utilities = 760000
        val transport = 162000
        val subscriptions = 250000
        val health = 1000000

        val budgets = listOf(
            Budget(GROCERIES, groceriesBudget, (groceriesBudget / weeksInCurrentMonth).toInt()),
            Budget(ENTERTAINMENT, 0, 0),
            Budget(RESTAURANTS, restaurantsBudget, (restaurantsBudget / weeksInCurrentMonth).toInt()),
            Budget(COFFEE, coffeeBudget, (coffeeBudget / weeksInCurrentMonth).toInt()),
            Budget(EDUCATION, education, (education / weeksInCurrentMonth).toInt()),
            Budget(UTILITIES, utilities, (utilities / weeksInCurrentMonth).toInt()),
            Budget(TRANSPORT, transport, (transport / weeksInCurrentMonth).toInt()),
            Budget(SUBSCRIPTIONS, subscriptions, (subscriptions / weeksInCurrentMonth).toInt()),
            Budget(HEALTH, health, (health / weeksInCurrentMonth).toInt())
        )
        try {
            val result =
                this@TransactionRepoImpl.database.getCollection<Budget>(BUDGETS_COLLECTION_NAME).insertMany(budgets)
//            Result.Success(transaction)
        } catch (e: MongoException) {
            System.err.println("Unable to insert due to an error: $e")
//            Result.Error(e)
        }
    }

    override suspend fun setBudget(budget: Budget): Result<Unit> =
        try {
            val result = this@TransactionRepoImpl.database.getCollection<Budget>(BUDGETS_COLLECTION_NAME).insertOne(budget)
            Result.Success(Unit)
        } catch (e: MongoException) {
            System.err.println("Unable to insert due to an error: $e")
            Result.Error(e)
        }


    override suspend fun getBudgets(): List<Budget> {
        return database.getCollection<Budget>(BUDGETS_COLLECTION_NAME).find().toList()
    }
}