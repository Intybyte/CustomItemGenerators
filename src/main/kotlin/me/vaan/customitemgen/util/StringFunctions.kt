package me.vaan.customitemgen.util

import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

fun String.isSlimefun() : Boolean {
    return this.startsWith('@')
}

fun String.getItemStack() : ItemStack {
    if (isSlimefun()) {
        val sfItem = Slimefun.getRegistry().slimefunItemIds[this.drop(1)]
        sfItem ?: throw RuntimeException("Slimefun item not found")
        return sfItem.item
    }

    val material = Material.getMaterial(this)
    material ?: throw RuntimeException("Vanilla item not found")
    return ItemStack(material, 1)
}

fun String.getRecipeType() : RecipeType? {
    val clazz = RecipeType::class.java
    val field = clazz.getField(this.uppercase()) ?: return null
    return field[null] as RecipeType
}

fun String.component() : Component {
    return Component.text(this)
}