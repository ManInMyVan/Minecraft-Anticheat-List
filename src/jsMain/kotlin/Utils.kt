import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.fetch.Request
import org.w3c.fetch.Response
import kotlin.js.Date

object Utils {
    val fourMonthsAgo = Date.now() - 10_368_000_000

    suspend fun fetch(input: String) = window.fetch(Request(input)).await()
    suspend fun Response.getJson() = JSON.parse<Array<String>>(text().await())
    suspend fun fetchJson(input: String) = fetch(input).getJson()
}
