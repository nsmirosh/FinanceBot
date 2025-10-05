package nick.mirosh

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
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
    private val telegramApiManager: TelegramApiManager,
) {
    private val _showKeyboard = MutableSharedFlow<Long>()
    val showKeyboard = _showKeyboard.asSharedFlow()

    private val _showError = MutableSharedFlow<Pair<Long, String>>()
    val showError = _showError.asSharedFlow()

    fun processCommand(message: Message) {
        val command = message.text.lowercase()
        val chatId = message.chatId
        when {
            command.contains(WEEKLY_STATUS_COMMAND) ->
                CoroutineScope(Dispatchers.Default).launch {
                    _showKeyboard.emit(chatId)
                }

            command.contains(SET_BUDGET_COMMAND) -> setBudget()

            else -> {
            }
        }
    }

    fun sendWeeklyReport(chatId: Long, category: Category) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val weeklyTransactions = transactionRepo.getCurrentWeekTransactions()
                val budgets = transactionRepo.getBudgets()
                val transactionsForCategory = weeklyTransactions.filter { it.category == category }
                val totalMoneySpentForTheWeek = transactionsForCategory.sumOf { it.sum }

                val budget = budgets.first { it.category == category }
                val calculatedWeekBudget = calculateThisWeekBudget(budget.amountForMonth)

                val moneyLeft = calculatedWeekBudget - totalMoneySpentForTheWeek

                val report = Report(
                    moneyLeft = moneyLeft,
                    weekBudget = calculatedWeekBudget,
                    category = category
                )
                telegramApiManager.sendPhoto(chatId, report)
            } catch (e: Exception) {
                val message = "Failed to send weekly report: ${e.message}"
                _showError.emit(chatId to message)
            }
        }
    }

    data class Report(
        val moneyLeft: Int,
        val weekBudget: Int,
        val category: Category
    )


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
}
