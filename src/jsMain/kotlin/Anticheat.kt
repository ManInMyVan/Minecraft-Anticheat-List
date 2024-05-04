class Anticheat(anticheat: Array<String>) {
    val name: String
    val platform: String
    var status: String?
    val versions: String
    val spigot: Int?
    val github: String?
    var rating: String?
    val price: String
    var links = arrayOf<URL>()
        private set

    init {
        val ac = anticheat.asDynamic()

        name = ac.name as String
        platform = ac.platform as String
        status = ac.status as? String
        versions = ac.versions as String
        spigot = ac.spigot as? Int
        github = ac.github as? String
        rating = ac.rating as? String
        price = ac.price as String

        (ac.links as Array<*>).forEach {
            links += URL(it.asDynamic().name as? String, it.asDynamic().url as String)
        }
    }

    data class URL(val name: String?, val url: String)
}
