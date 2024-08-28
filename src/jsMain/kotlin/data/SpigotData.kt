package data

class SpigotData(val rating: String, val lastUpdate: Double) {
    constructor(data: dynamic) : this(
        "${(data.rating.average as Double * 20).toInt()}%, ${data.rating.count} ${if (data.rating.count == 1) "rating" else "ratings"}",
        data.updateDate as Double * 1000
    )
}
