package data

import kotlin.js.Date

class GitHubData(val private: Boolean, val archived: Boolean, val lastPush: Double) {

    constructor(data: dynamic) : this(data.private as Boolean, data.archived as Boolean, Date.parse(data.pushed_at as String))

    companion object {
        private var warnedAboutRateLimit = false

        fun warnAboutRateLimit() {
            if (!warnedAboutRateLimit)
                console.warn("You have been rate limited by github, some things may be broken!")
            warnedAboutRateLimit = true
        }
    }
}
