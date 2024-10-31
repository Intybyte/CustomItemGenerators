package me.vaan.customitemgen.data

import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.Location

data class MachineData(
    var itemProduced: Int = 0,
    var recipeExecuted: Int = 0
) {
    companion object {
        const val KEY_ITEM_PRODUCED = "item-produced"
        const val KEY_RECIPE_EXECUTED = "recipe-executed"
    }

    constructor(location: Location) : this(
        BlockStorage.getLocationInfo(location, KEY_ITEM_PRODUCED)?.toInt() ?: 0,
        BlockStorage.getLocationInfo(location, KEY_RECIPE_EXECUTED)?.toInt() ?: 0
    )

    fun serialize(location: Location) {
        BlockStorage.addBlockInfo(location, KEY_ITEM_PRODUCED, itemProduced.toString())
        BlockStorage.addBlockInfo(location, KEY_RECIPE_EXECUTED, recipeExecuted.toString())
    }
}