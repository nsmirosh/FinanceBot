package nick.mirosh

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import nick.mirosh.repository.Result
import nick.mirosh.repository.TransactionRepo
import org.telegram.telegrambots.meta.api.objects.Update

class TransactionManager(
    private val repo: TransactionRepo,
) {
    private val _message = MutableSharedFlow<Pair<Long, String>?>()
    val message = _message.asSharedFlow()

    suspend fun processTransaction(update: Update) {

        var transaction: Transaction
        try {
            transaction = parseUpdate(update)
        } catch (
            e: IllegalArgumentException
        ) {
            _message.emit(update.message.chatId to "Не понимать, попробуй ещё раз \n Error: ${e.message}")
            return
        }

        val message = when (val result = repo.createTransaction(transaction)) {
            is Result.Success ->
                with(result.data) {
                    "Зописав! \n " +
                            "cумма = ${sum / 100} \n " +
                            "категория = $category \n " +
                            "описание = $description \n " +
                            "валюта = $currency \n"
                }

            is Result.Error -> "Поняв, но не записав чет \n Error: ${result.throwable.message}"
        }
        _message.emit(update.message.chatId to message)
    }
}