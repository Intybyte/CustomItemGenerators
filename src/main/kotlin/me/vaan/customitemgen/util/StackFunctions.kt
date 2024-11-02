package me.vaan.customitemgen.util

import me.vaan.customitemgen.CustomItemGenerators
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType


fun ItemStack.mark() : ItemStack {
    val copy = this.clone()
    val meta = copy.itemMeta
    meta.persistentDataContainer.set(CustomItemGenerators.key, PersistentDataType.BOOLEAN, true)
    copy.itemMeta = meta
    return copy
}

fun ItemStack.demark() : ItemStack {
    val meta = this.itemMeta
    meta.persistentDataContainer.remove(CustomItemGenerators.key)
    this.itemMeta = meta
    return this
}
