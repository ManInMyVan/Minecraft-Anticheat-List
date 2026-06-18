package data

class SpigotData(data: dynamic) {
    val lastUpdate: Double = data.updateDate as Double * 1000
}
