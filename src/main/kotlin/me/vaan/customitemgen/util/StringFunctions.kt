package me.vaan.customitemgen.util

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
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

fun Any.component() : TextComponent {
    return toString().component()
}

fun String.component() : TextComponent {
    return Component.text(this)
}

fun Material.getDefaultName() : TextComponent {
    return this.toString()
        .lowercase()
        .replace('_', ' ')
        .capitalizeWords()
        .component()
}

fun String.capitalizeWords() : String {
    val words = this.split(' ')
    for (word in words) {
        word.replaceFirstChar { it.titlecase() }
    }

    return this.split(' ').joinToString(" ") { word ->
        word.replaceFirstChar { it.titlecase() }
    }
}