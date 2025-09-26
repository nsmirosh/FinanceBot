package nick.mirosh

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import nick.mirosh.repository.TransactionRepo
import org.telegram.telegrambots.meta.api.objects.Update

import nick.mirosh.repository.Result

class TransactionManager(
    private val repo: TransactionRepo,
) {
    private val _message = MutableStateFlow<Pair<Long, String>?>(null)
    val message = _message.asStateFlow()

    fun processTransaction(update: Update) {

        var transaction: Transaction
        try {
            transaction = parseUpdate(update)
        } catch (
            e: IllegalArgumentException
        ) {
            _message.value = update.message.chatId to "Не понимать, попробуй ещё раз \n Error: ${e.message}"
            return
        }

        runBlocking {
            val message = when (val result = repo.createTransaction(transaction)) {

                is Result.Success ->

                    with(result.data) {
                        "Зописав! \n " +
                                "cумма = $sum \n " +
                                "категория = $category \n " +
                                "описание = $description \n " +
                                "валюта = $currency \n"
                    }

                is Result.Error -> "Поняв, но не записав чет \n Error: ${result.throwable.message}"
            }
            _message.value = update.message.chatId to message
        }
    }
}