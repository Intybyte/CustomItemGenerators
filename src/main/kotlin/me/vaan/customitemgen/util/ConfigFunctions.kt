package me.vaan.customitemgen.util

import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe
import me.vaan.customitemgen.generator.GenEntry
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.inventory.ItemStack


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

fun FileConfiguration.getRecipe(id: String) : Array<ItemStack?> {
    val stringGrid = this.getStringList("$id.recipe.grid").joinToString("")
    val grid = stringGrid.toCharArray()

    val mapperList = this.getList("$id.recipe.mapper") as List<List<Any>>
    val mapperMap = mapperList.associate {
        val key = (it[0] as String)[0]
        val stringValue = it[1] as String
        val value = stringValue.getItemStack()
        key to value
    }

    return grid.map { mapperMap[it] }.toTypedArray()
}

fun FileConfiguration.getProduction(id: String) : MutableList<GenEntry> {
    val productionList = this.getList("$id.production") as List<List<Any>>

    val genList = productionList.map {
        val resultString = it[0] as String
        val result = resultString.getItemStack()
        val time = it[1] as Int

        val recipe = MachineRecipe(time, arrayOf(result), arrayOf(result))

        val energy = it[2] as Int
        GenEntry(recipe, energy)
    }

    return ArrayList(genList)
}