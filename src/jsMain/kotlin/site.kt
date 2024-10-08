import Utils.fetchJson
import kotlinx.browser.document
import data.Anticheat
import data.Status.Active
import data.Status.Unavailable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun main() {
    val anticheats: MutableList<Anticheat> = mutableListOf()

    coroutineScope {
        for (json in fetchJson("./anticheats.json")) launch {
            val anticheat = Anticheat(JSON.parse(JSON.stringify(json)))

            try {
                anticheat.update()
            } catch (error: Throwable) {
                console.error("Uncaught exception while getting data for ${anticheat.name}: ${error.message}")
                error.printStackTrace()
            }

            anticheats += anticheat
        }
    }

    document.getElementById("number-of-anticheats")!!.innerHTML = "Number of Anticheats: ${anticheats.size}"

    anticheats.sortedWith(anticheatSorter).forEach(display)
}

/**
 * Sorts in the following order:
 * 1. Active
 * 2. Inactive, Unmaintained, Discontinued, Unknown, Old
 * 3. Unavailable
 */
val anticheatSorter = Comparator<Anticheat> { a, b ->
    val statusA = when (a.status) {
        Active -> 0
        Unavailable -> 2
        else -> 1
    }

    val statusB = when (b.status) {
        Active -> 0
        Unavailable -> 2
        else -> 1
    }

    return@Comparator if (statusA == statusB) a.name.compareTo(b.name) else statusA.compareTo(statusB)
}

val display = { anticheat: Anticheat ->
    document.getElementById("anticheat-table")!!.innerHTML +=
        """<tr>
            <td>${anticheat.name}</td>
            <td>${anticheat.platforms.joinToString()}</td>
            <td>${anticheat.status}</td>
            <td>${anticheat.versions}</td>
            <td>${anticheat.rating}</td>
            <td>${anticheat.price}</td>
            <td>${anticheat.links
                .filter { !it.hidden }
                .sortedWith { a, b ->
                    if (a.name == null || b.name == null) return@sortedWith 0
                    return@sortedWith -a.name.compareTo(b.name)
                }.joinToString(", ") { "<a href=\"${it.url}\">${it.name}</a>" }
            }</td>
            </tr>
            """
}
