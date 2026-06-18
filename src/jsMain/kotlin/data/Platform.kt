package data

import data.Platform.Edition.Bedrock
import data.Platform.Edition.Java

enum class Platform(private val edition: Edition?) {
    Unknown(null),
    Bukkit(Java),
    Sponge(Java),
    Skript(Java),
    Minestom(Java),
    Datapack(Java),
    Fabric(Java),
    Forge(Java),

    Nukkit(Bedrock),
    BehaviorPack(Bedrock),
    PocketMine(Bedrock),
    ;

    enum class Edition {
        Java, Bedrock
    }

    private val display by lazy {
        val name = if (name == "BehaviorPack") "Behavior Pack" else name
        if (edition != null) {
            "$name ($edition)"
        } else name
    }

    override fun toString() = display
}
