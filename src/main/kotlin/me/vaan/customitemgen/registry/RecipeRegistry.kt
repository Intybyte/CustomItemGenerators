package me.vaan.customitemgen.registry

import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun

/**
 * This class will exist until slimefun decides to implement some way to keep track of
 * all the recipe types. It is useful to keep track of the recipe types added by other addons
 */
object RecipeRegistry {
    private val registry = HashMap<String, RecipeType>()

    init {
        val items = Slimefun.getRegistry().slimefunItemIds.values
        for (item in items) {
            registry[item.recipeType.key.key.lowercase()] = item.recipeType
        }
    }

    operator fun get(key: String) : RecipeType? {
        return registry[key.lowercase()]
    }
}