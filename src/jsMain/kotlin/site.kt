import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.fetch.Request
import kotlinx.coroutines.await
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.w3c.fetch.Response
import kotlin.js.Date
import kotlin.math.max

suspend fun main() {
    val anticheats: Array<Anticheat> = run {
        val temp = mutableListOf<Anticheat>()
        fetchJson("./anticheats.json").forEach { temp += Anticheat(JSON.parse(JSON.stringify(it))) }
        temp.toTypedArray()
    }

    document.getElementById("number-of-anticheats")!!.innerHTML = "Number of Anticheats: ${anticheats.size}"

    // Update ratings & statuses async
    coroutineScope {
        var warnedAboutRateLimit = false
        for (it in anticheats) {
            if (it.rating != null && it.status != null)
                continue

            launch {
                val spigotData = if (it.spigot == null) null else fetchJson("https://api.spiget.org/v2/resources/" + it.spigot).asDynamic()

                if (it.rating == null) {
                    it.rating = if (spigotData == null) "Unknown"
                    else "${((spigotData.rating.average as Double) * 20).toInt()}%, ${(spigotData.rating.count as Int)} ${if ((spigotData.rating.count as Int) == 1) "rating" else "ratings"}"
                }

                if (it.status == null) {
                    val githubResponse = if (it.github == null) null else fetch("https://api.github.com/repos/" + it.github)

                    val githubData = if (githubResponse == null) null else if (githubResponse.status == 403.toShort()) {
                        if (!warnedAboutRateLimit)
                            console.warn("You have been rate limited by github, some things may be broken!")
                        warnedAboutRateLimit = true
                        null
                    } else githubResponse.getJson().asDynamic()

                    it.status = when {
                        githubData != null && (githubData.private as Boolean) -> "Unavailable"
                        githubData != null && (githubData.archived as Boolean) -> "Discontinued"
                        // 4 months (1000ms * 60s * 60m * 24h * 30d * 4mo)
                        10_368_000_000 > Date.now() - max(
                            if (spigotData == null) 0.0 else (spigotData.updateDate as Double) * 1000,
                            if (githubData == null) 0.0 else Date.parse(githubData.pushed_at as String)
                        ) -> "Active"
                        else -> "Old"
                    }
                }
            }
        }
    }

    anticheats.sortedWith { a, b ->
        // Active
        // Inactive, Unmaintained, Discontinued, Unknown, Old
        // Unavailable

        val statusA = when (a.status) {
            "Active" -> 0
            "Unavailable" -> 2
            else -> 1
        }

        val statusB = when (b.status) {
            "Active" -> 0
            "Unavailable" -> 2
            else -> 1
        }

        if (statusA == statusB) (a.name as String).compareTo(b.name as String) else statusA.compareTo(statusB)
    }.forEach {
        val links = mutableListOf<String>()
        it.links.sortedWith { a, b ->
            if (a.name == null || b.name == null) return@sortedWith 0
            return@sortedWith -a.name.compareTo(b.name)
        }.forEach { link -> if (link.name != null) links += "<a href=\"${link.url}\">${link.name}</a>" }

        document.getElementById("anticheat-table")!!.innerHTML +=
            """<tr>
            <td>${it.name}</td>
            <td>${it.platform}</td>
            <td>${it.status}</td>
            <td>${it.versions}</td>
            <td>${it.rating}</td>
            <td>${it.price}</td>
            <td>${links.joinToString(", ")}</td>
            </tr>
            """
    }
}

suspend fun fetch(input: String) = window.fetch(Request(input)).await()
suspend fun Response.getJson() = JSON.parse<Array<String>>(text().await())
suspend fun fetchJson(input: String) = fetch(input).getJson()
fun toggleTheme() {
   document.body!!.classList.toggle("dark-mode")
   document.body!!.classList.toggle("light-mode")
}
