package me.vaan.customitemgen.file

import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun

/**
 * This class will exist until slimefun decides to implement some way to keep track of
 * all the recipe types. It is useful to keep track of the recipe types added by other addons
 */
object RecipeRegistry {
    private val registry = HashSet<RecipeType>()

    fun load() {
        val items = Slimefun.getRegistry().slimefunItemIds.values
        for (item in items) {
            registry.add(item.recipeType)
        }
    }

    operator fun get(key: String) : RecipeType? {
        val lowered = key.lowercase()
        return registry.find {
            it.key.key.lowercase() == lowered
        }
    }
}