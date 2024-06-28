package data

import data.Edition.Bedrock
import data.Edition.Java

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

    private val display by lazy {
        val name = if (this == BehaviorPack) "Behavior Pack" else name
        if (edition != null) {
            "$name ($edition)"
        } else name
    }

    override fun toString() = display
}
