package me.vaan.customitemgen

import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
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

fun FileConfiguration.getStack(id: String) : ItemStack {

    val blockString = this.getString("$id.block") ?: throw RuntimeException("$id no block found")
    val blockMaterial = Material.getMaterial(blockString) ?: throw RuntimeException("$id machine block material not found")

    val name = this.getString("$id.name")?.component() ?: Component.text("")
    val lore = this.getStringList("$id.lore").map { it.component() }

    val stack = ItemStack(blockMaterial, 1)
    stack.editMeta {
        it.displayName( name )
        it.lore( lore )
    }

    return stack
}