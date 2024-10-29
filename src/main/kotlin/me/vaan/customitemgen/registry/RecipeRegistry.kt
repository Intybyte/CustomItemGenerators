package me.vaan.customitemgen.registry

import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun

/**
 * This class will exist until slimefun decides to implement some way to keep track of
 * all the recipe types. It is useful to keep track of the recipe types added by other addons
 */
object RecipeRegistry : MutableMap<String, RecipeType> by HashMap() {
    init {
        val items = Slimefun.getRegistry().slimefunItemIds.values
        for (item in items) {
            val recipeType = item.recipeType
            this[recipeType.key.key.lowercase()] = recipeType
        }
    }
}