package nick.mirosh

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nick.mirosh.repository.TransactionRepo
import org.telegram.telegrambots.meta.api.objects.Message
import java.time.LocalDate
import java.time.ZoneId

const val WEEKLY_STATUS_COMMAND = "weekly_status"
const val SET_BUDGET_COMMAND = "set_budget"

class CommandManager(private val transactionRepo: TransactionRepo) {

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
            val budgets = transactionRepo.getMonthlyBudgets()
            val weeksInMonth = getWeeksInCurrentMonth()


            //TODO get transaction for the week only for week status


            val mapByCategory = transactions.groupBy { it.category }

            val report = mutableListOf<String>()
            report.add("Weekly Status Report:")
            report.add("This month has ${"%.2f".format(weeksInMonth)} weeks")
            report.add("-------------------------------------")
            val categoryHeader = "    Category    "
            val totalAmountHeader = " Total "
            val monthlyBudget = "Monthly Budget"
            val moneyLeftForTheWeek = " For This Week "
            report.add("|$categoryHeader|$totalAmountHeader|$monthlyBudget|$moneyLeftForTheWeek|")
            report.add("-------------------------------------")

            for ((category, transactions) in mapByCategory) {
                val totalAmount = transactions.sumOf { it.sum }
                val totalAmountLength = totalAmount.toString().length

                val categoryPaddingNeededForEachSide = (categoryHeader.length - category.length) / 2
                val categoryFormatted = if (categoryPaddingNeededForEachSide > 0) {
                    category
                        .padStart(category.length + categoryPaddingNeededForEachSide).let {
                            it.padEnd(it.length + categoryPaddingNeededForEachSide)
                        }
                } else category


                val totalAmountPaddingNeededForEachSide = (totalAmountHeader.length - totalAmountLength) / 2
                val totalAmountFormatted = if (totalAmountPaddingNeededForEachSide > 0)
                    totalAmount.toString()
                        .padStart(totalAmountPaddingNeededForEachSide)
                        .padEnd(totalAmountPaddingNeededForEachSide)
                else totalAmount.toString()


                var moneysLeftForTheWeek: String? = null
                val budget = budgets.values.firstOrNull { it.first.uppercase() == category.uppercase() }?.let {
//                    moneysLeftForTheWeek = (it.second  / weeksInMonth) - totalAmount
                    it.second.toString()
                } ?: "No budget"

//                moneysLeftForTheWeek = moneysLeftForTheWeek?.let { "%.2f".format(it.toFloat()) } ?: "No budget"

                report.add(
                    "| $categoryFormatted | $totalAmountFormatted | $budget | $moneysLeftForTheWeek |"
                )
            }
            report.add("-------------------------------------")
            report.add("Total: ${transactions.sumOf { it.sum }}")
            _report.value = chatId to report
        }
    }

    private fun getWeeksInCurrentMonth(): Float {
        val zone = ZoneId.of("Asia/Bangkok")
        val today = LocalDate.now(zone)
        val lastOfMonth = today.withDayOfMonth(today.lengthOfMonth())

        val totalDaysInMonth = lastOfMonth.dayOfMonth
        return totalDaysInMonth / 7.0f
    }

    private fun setBudget() {
        CoroutineScope(Dispatchers.IO).launch {
            transactionRepo.setBudget()
        }
    }
}
