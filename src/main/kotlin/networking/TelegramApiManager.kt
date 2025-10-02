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


fun buildChartRequestBody() =
    Chart(
        type = "bar",
        data = ChartData(
            labels = listOf(""),
            datasets = listOf(
                Dataset(
                    backgroundColor = "#8ac926",
                    label = "Money Left",
                    data = listOf(80)
                ),

                Dataset(
                    backgroundColor = "#8ac926",
                    label = "Money Not Left",
                    data = listOf(60)
                )
            )
        ),

        options = Options(
            plugins = Plugins(
                title = Title(
                    display = true,
                    text = "Coffee"
                )
            )
        )
    )


class TelegramApiManager(private val httpClient: HttpClient) {

    fun sendPhoto(chatId: String) {
        val pictureUrl = buildUrl {
            protocol = URLProtocol.HTTPS
            host = "quickchart.io"
            path("chart")
            parameters.append("v", "4")

            parameters.append(
                "c",
                Json.encodeToString(
                    Chart.serializer(),
                    buildChartRequestBody()
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
                    io.ktor.http.ContentType.Application.Json
                )
                setBody(
                    TelegramRequestBody(
                        chatId = chatId,
                        photo = pictureUrl.toString()
                    )
                )
            }
        }
    }

}