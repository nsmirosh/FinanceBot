package nick.mirosh

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nick.mirosh.repository.TransactionRepo
import org.telegram.telegrambots.meta.api.objects.Message


const val weeklyStatus = "weekly_status"
const val defaultCurrency = "default_currency"

class CommandProcessor(private val transactionRepo: TransactionRepo) {

    private val _report = MutableStateFlow<Pair<Long, List<String>>?>(null)
    val report = _report.asStateFlow()

    fun processCommand(message: Message) {
        val command = message.text.lowercase()
        val chatId = message.chatId
        when {
            command.contains(weeklyStatus) -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val transactions = transactionRepo.getCurrentWeekTransactions()
                    transactions.forEach { println("transaction = $it") }

                    val mapByCategory = transactions.groupBy { it.category }

                    val report = mutableListOf<String>()
                    for ((category, transactions) in mapByCategory) {
                        val totalAmount = transactions.sumOf { it.sum }
                        report.add("Category: $category, Total Amount: $totalAmount")
                        println(" processCommand = Category: $category, Total Amount: $totalAmount")
                    }
                    _report.value = chatId to report
                }
            }

            else -> {
            }
        }
    }


    private fun processDefaultCurrency() {

    }
}
