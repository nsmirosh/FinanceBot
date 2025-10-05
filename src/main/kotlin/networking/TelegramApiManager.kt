package nick.mirosh.networking

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import nick.mirosh.BOT_TOKEN
import nick.mirosh.CommandManager


@Serializable
data class TelegramRequestBody(
    @SerialName("chat_id") val chatId: String,
    val photo: String
)

/*
https://www.chartjs.org/docs/2.9.4/configuration/title.html
 */

@Serializable
data class ChartRequestBody(
    @SerialName("backgroundColor") val backgroundColor: String,
    val width: Int,
    val height: Int,
    @SerialName("devicePixelRatio") val devicePixelRatio: Double,
    val chart: Chart,
)

@Serializable
data class Options(
    val plugins: Plugins
)

@Serializable
data class Plugins(
    val title: Title
)

@Serializable
data class Title(
    val display: Boolean,
    val text: String
)

@Serializable
data class Chart(
    val type: String,
    val data: ChartData,
    val options: Options
)

@Serializable
data class ChartData(
    val labels: List<String>,
    val datasets: List<Dataset>
)

@Serializable
data class Dataset(
    val backgroundColor: String,
    val label: String,
    val data: List<Int>
)


fun buildChartRequestBody(
    report: CommandManager.Report,
) =
    with(report) {
        Chart(
            type = "bar",
            data = ChartData(
                labels = listOf(""),
                datasets = listOf(
                    Dataset(
                        backgroundColor = "#8ac926",
                        label = "Money Left = ${moneyLeft / 100} ",
                        data = listOf(moneyLeft / 100)
                    ),

                    Dataset(
                        backgroundColor = "#ffb703",
                        label = "Budget = ${weekBudget / 100}",
                        data = listOf(weekBudget / 100)
                    )
                )
            ),

            options = Options(
                plugins = Plugins(
                    title = Title(
                        display = true,
                        text = category.displayName ?: "Unknown"
                    )
                )
            )
        )
    }


class TelegramApiManager(private val httpClient: HttpClient) {

    fun sendPhoto(
        chatId: Long,
        report: CommandManager.Report,
        ) {

        val pictureUrl = buildUrl {
            protocol = URLProtocol.HTTPS
            host = "quickchart.io"
            path("chart")
            parameters.append("v", "4")
            parameters.append(
                "c",
                Json.encodeToString(
                    Chart.serializer(),
                    buildChartRequestBody(report)
                )
            )
        }
        CoroutineScope(Dispatchers.IO).launch {
            httpClient.post {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "api.telegram.org"
                    path("bot$BOT_TOKEN/sendPhoto")
                }
                contentType(
                    ContentType.Application.Json
                )
                setBody(
                    TelegramRequestBody(
                        chatId = chatId.toString(),
                        photo = pictureUrl.toString()
                    )
                )
            }
        }
    }

}


/*

- determine budget per day.

if Sunday of current week month is different from current Month. Determine the amount of days left in this month and
and multiply daily budget by that amount of days.

else if Monday month is different from current Month. Determine when the current month starts and get that amount of days
from current day

else  simply multiply by 7 and return because we're in a full week within a month
 */

