package data

import Utils.fetch
import Utils.fourMonthsAgo
import Utils.getJson
import data.Status.*

class Anticheat(data: dynamic) {
    val name = data.name as String
    val platform = data.platform as String
    var status = (data.status as? String)?.let { Status.valueOf(it) }
    val versions = data.versions as String
    private val spigot = data.spigot as? Int
    private val github = data.github as? String
    var rating = data.rating as? String
    val price = data.price as String
    val links = (data.links as Array<dynamic>).map { URL(it.name as? String, it.url as String) }.toTypedArray()

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
        return SpigotData(fetch("https://api.spiget.org/v2/resources/$spigot").getJson().asDynamic())
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
}
