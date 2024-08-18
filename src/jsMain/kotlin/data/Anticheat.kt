package data

import Utils.fetch
import Utils.fourMonthsAgo
import Utils.getJson
import data.Status.*

class Anticheat(
    val name: String,
    val platforms: Array<Platform>,
    var status: Status?,
    val versions: String,
    private val spigot: Int?,
    private val github: String?,
    var rating: String?,
    val price: String,
    val links: Array<Link>
) {
    private suspend fun getGitHubData(): GitHubData? {
        github ?: return null

        val response = fetch("https://api.github.com/repos/$github")

        when (response.status.toInt()) {
            403 -> {
                GitHubData.warnAboutRateLimit()
                return null
            }
            404 -> return GitHubData(private = true, archived = false, lastPush = 0.0)
        }

        return GitHubData(response.getJson().asDynamic())
    }

    private suspend fun getSpigotData(): SpigotData? {
        spigot ?: return null

        val response = fetch("https://api.spiget.org/v2/resources/$spigot")

        when (response.status.toInt()) {
            404 -> return null
        }

        return SpigotData(response.getJson().asDynamic())
    }

    suspend fun update() {
        if (rating != null && status != null) return

        val spigotData = getSpigotData()

        if (rating == null) {
            rating = spigotData?.rating ?: "Unknown"
        }

        if (status == null) {
            // avoid using GitHub api because rate limits
            if (spigotData != null && fourMonthsAgo < spigotData.lastUpdate) {
                status = Active
                return
            }

            if (spigotData == null && spigot != null && github == null) {
                status = Unavailable
                return
            }

            val githubData = getGitHubData()

            if (githubData == null) {
                status = Old
                return
            }

            status = when {
                githubData.private -> Unavailable // TODO: does this even work?
                githubData.archived -> Discontinued
                fourMonthsAgo < githubData.lastPush -> Active
                else -> Old
            }
        }
    }

    constructor(data: dynamic) : this(
        data.name as String,
        (data.platforms as Array<String>).map(Platform::valueOf).toTypedArray(),
        (data.status as? String)?.let(Status::valueOf),
        data.versions as String,
        data.spigot as? Int,
        data.github as? String,
        data.rating as? String,
        data.price as String,
        (data.links as Array<dynamic>).map { Link(it.name as? String, it.url as String, it.name as? String == null) }.toTypedArray()
    )

    init { values += this }
    companion object {
        val values = mutableSetOf<Anticheat>()
    }
}
