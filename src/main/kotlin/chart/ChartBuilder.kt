package nick.mirosh.chart

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChartBuilder(private val client: HttpClient) {

    fun requestChart() {
        val url = "https://quickchart.io/chart/render/zm-847e2578-9bf6-4b35-bbd1-e12cd674b15f"

        CoroutineScope(Dispatchers.IO).launch {
            client.get(url)
        }
    }
}