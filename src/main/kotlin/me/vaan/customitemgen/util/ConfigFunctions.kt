package me.vaan.customitemgen.util

import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe
import me.vaan.customitemgen.CustomItemGenerators
import me.vaan.customitemgen.generator.GenEntry
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.inventory.ItemStack


fun FileConfiguration.getBlock(id: String) : ItemStack {
    val blockString = this.getString("$id.block") ?: throw RuntimeException("$id no block found")

    val name = this.getString("$id.name")?.component() ?: Component.text("")
    val lore = this.getStringList("$id.lore").map { it.replace("&", "§").component() }

    val stack: ItemStack

    if (blockString.startsWith("SKULL")) {
        try {
            val skullIDString = blockString.split("_")[1]
            stack = SlimefunUtils.getCustomHead(skullIDString)
        } catch (e: Exception) {
            throw RuntimeException("Error while reading skullID from $id.block, format is: SKULL_ID")
        }
    } else {
        val blockMaterial = Material.getMaterial(blockString) ?: throw RuntimeException("$id machine block material not found")
        stack = ItemStack(blockMaterial, 1)
    }

    stack.editMeta {
        it.displayName(name)
        it.lore(lore)
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
        if (value.amount != 1) {
            CustomItemGenerators.instance.logger.warning("""
                You created a recipe using an amount different than 1, 
                I am all for the f around and find out, but what you are trying to 
                do is unsupported behaviour, so if the recipe breaks don't blame me ¯\_(ツ)_/¯.""".trimIndent())
        }

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