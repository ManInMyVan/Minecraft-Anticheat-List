package data

enum class Platform(display: String? = null) {
    Unknown,
    Bukkit,
    Sponge,
    Skript,
    Minestom,
    Datapack,
    Fabric,
    Forge;

    private val display = display ?: name

    override fun toString() = display
}
