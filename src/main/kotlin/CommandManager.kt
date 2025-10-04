package nick.mirosh

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nick.mirosh.chart.ChartBuilder
import nick.mirosh.networking.TelegramApiManager
import nick.mirosh.repository.TransactionRepo
import nick.mirosh.utils.Category
import org.telegram.telegrambots.meta.api.objects.Message
import java.time.LocalDate
import java.time.ZoneId

const val WEEKLY_STATUS_COMMAND = "weekly_status"
const val SET_BUDGET_COMMAND = "set_budget"

class CommandManager(
    private val transactionRepo: TransactionRepo,
    private val telegramApiManager: TelegramApiManager
) {

    private val _report = MutableStateFlow<Pair<Long, List<String>>?>(null)
    val report = _report.asStateFlow()

    fun processCommand(message: Message) {
        val command = message.text.lowercase()
        val chatId = message.chatId

        println("Processing command: $command")
        when {
            command.contains(WEEKLY_STATUS_COMMAND) -> {
                try {
                    getWeeklyStatus(chatId)
                } catch (e: Exception) {
                    println("Error = ${e.message}")
                    e.printStackTrace()
                }
            }

            command.contains(SET_BUDGET_COMMAND) -> setBudget()

            else -> {
            }
        }
    }

    private fun getWeeklyStatus(chatId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val weeklyTransactions = transactionRepo.getCurrentWeekTransactions()
            val budgets = transactionRepo.getBudgets()

            val mapByCategory = weeklyTransactions.groupBy { it.category }

            val report = mutableListOf<String>()

            for ((category, transactions) in mapByCategory) {
                val totalMoneySpentForTheWeek = transactions.sumOf { it.sum }
                val budget = budgets.first { it.category == category }
                val moneyLeft = budget.amountForCurrentWeek - totalMoneySpentForTheWeek
                if (category == Category.COFFEE) {
                    telegramApiManager.sendPhoto(chatId.toString(), moneyLeft, budget)
                }
            }
            _report.value = chatId to report
        }
    }


    fun getWeeksInCurrentMonth(): Float {
        val zone = ZoneId.of("Asia/Bangkok")
        val today = LocalDate.now(zone)
        val lastOfMonth = today.withDayOfMonth(today.lengthOfMonth())

        val totalDaysInMonth = lastOfMonth.dayOfMonth
        return totalDaysInMonth / 7.0f
    }

    private fun setBudget() {
        CoroutineScope(Dispatchers.IO).launch {
            transactionRepo.setHardCodedBudgets()
        }
    }

//    private fun formatting() {
//
//        val totalAmountLength = totalAmount.toString().length
//
//        val categoryPaddingNeededForEachSide = (categoryHeader.length - category.length) / 2
//        val categoryFormatted = if (categoryPaddingNeededForEachSide > 0) {
//            category
//                .padStart(category.length + categoryPaddingNeededForEachSide).let {
//                    it.padEnd(it.length + categoryPaddingNeededForEachSide)
//                }
//        } else category
//
//
//        val totalAmountPaddingNeededForEachSide = (totalAmountHeader.length - totalAmountLength) / 2
//        val totalAmountFormatted = if (totalAmountPaddingNeededForEachSide > 0)
//            totalAmount.toString()
//                .padStart(totalAmountPaddingNeededForEachSide)
//                .padEnd(totalAmountPaddingNeededForEachSide)
//        else totalAmount.toString()
//    }
}
