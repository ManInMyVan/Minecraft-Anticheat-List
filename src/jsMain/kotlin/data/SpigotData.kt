package data

class SpigotData(data: dynamic) {
    val rating: String
    val lastUpdate = data.updateDate as Double * 1000

    init {
        val averageRating = (data.rating.average as Double * 20).toInt()
        val ratings = data.rating.count as Int

        rating = "$averageRating%, $ratings ${if (ratings == 1) "rating" else "ratings"}"
    }
}
