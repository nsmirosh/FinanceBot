package nick.mirosh

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nick.mirosh.repository.TransactionRepo
import org.telegram.telegrambots.meta.api.objects.Message

const val WEEKLY_STATUS_COMMAND = "weekly_status"
const val SET_BUDGET_COMMAND = "set_budget"

class CommandProcessor(private val transactionRepo: TransactionRepo) {

    private val _report = MutableStateFlow<Pair<Long, List<String>>?>(null)
    val report = _report.asStateFlow()

    fun processCommand(message: Message) {
        val command = message.text.lowercase()
        val chatId = message.chatId

        println("Processing command: $command")
        when {
            command.contains(WEEKLY_STATUS_COMMAND) -> getWeeklyStatus(chatId)
            command.contains(SET_BUDGET_COMMAND) -> setBudget()

            else -> {
            }
        }
    }

    private fun getWeeklyStatus(chatId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val transactions = transactionRepo.getCurrentWeekTransactions()
            println("Transactions: $transactions")

            val mapByCategory = transactions.groupBy { it.category }

            val report = mutableListOf<String>()
            report.add("----------------------------------------------")
            report.add("|   Category   |   Total Amount   |")
            report.add("----------------------------------------------")

            for ((category, transactions) in mapByCategory) {
                val totalAmount = transactions.sumOf { it.sum }
                val categoryFormatted = category.padStart((category.length + 12) / 2).padEnd(12)
                val totalAmountFormatted = totalAmount.toString().padStart((totalAmount.toString().length + 16) / 2).padEnd(16)
                report.add("| $categoryFormatted | $totalAmountFormatted |")
            }
            report.add("----------------------------------------------")
            report.add("Total: ${transactions.sumOf { it.sum }}")
            _report.value = chatId to report
        }
    }

    private fun setBudget() {
        CoroutineScope(Dispatchers.IO).launch {
            transactionRepo.setBudget()
        }
    }
}
