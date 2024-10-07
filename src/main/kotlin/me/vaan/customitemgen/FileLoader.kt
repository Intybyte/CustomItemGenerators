package me.vaan.customitemgen

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.File

object FileLoader {
    fun loadFiles(file: File) {
        val machines = YamlConfiguration()
        machines.load(file)

        for (id in machines.getKeys(false)) {
            if (!machines.getBoolean("$id.enabled")) {
                continue
            }

            val blockString = machines.getString("$id.block") ?: throw RuntimeException("$id machine has no block")
            val blockMaterial = Material.getMaterial(blockString) ?: throw RuntimeException("$id machine block material not found")

            val name = machines.getString("$id.name") ?: ""
            val lore = machines.getStringList("$id.lore")

            val energyCapacity = machines.getInt("$id.energy-capacity")

            val progressBarString = machines.getString("$id.progress-bar") ?: throw RuntimeException("$id machine progress entry not found")
            val progressBarMaterial = Material.getMaterial(progressBarString) ?: throw RuntimeException("$id machine progress bar material not found")

            val machineItem = SlimefunItemStack(id, blockMaterial, name, *lore.toTypedArray())

            val recipeTypeString = machines.getString("$id.recipe.type") ?: throw RuntimeException("$id machine recipe type not specified")
            val recipeType = recipeTypeString.getRecipeType() ?: throw RuntimeException("$id machine recipe type not found")

            val recipe = getRecipe(machines, id)
            if (recipe.isEmpty()) throw RuntimeException("$id has invalid/empty recipe")

            val production = getProduction(machines, id)
            if (production.isEmpty()) throw RuntimeException("$id has invalid/empty production list")

            val maxProduction = production.maxOf { it.energy }
            if (energyCapacity < maxProduction) throw RuntimeException("$id the energy-capacity must always be above the energy production cost")

            ItemGenerator(CustomItemGenerators.group, machineItem, recipeType, recipe, ItemStack(progressBarMaterial), production)
                .setCapacity(energyCapacity)
                .register(CustomItemGenerators.instance)
        }
    }

    private fun getRecipe(file: FileConfiguration, id: String) : Array<ItemStack?> {
        val stringGrid = file.getStringList("$id.recipe.grid").joinToString("")
        val grid = stringGrid.toCharArray()

        val mapperList = file.getList("$id.recipe.mapper") as List<List<Any>>
        val mapperMap = mapperList.associate {
            val key = (it[0] as String)[0]
            val stringValue = it[1] as String
            val value = stringValue.getItemStack()
            key to value
        }

        return grid.map { mapperMap[it] }.toTypedArray()
    }

    private fun getProduction(file: FileConfiguration, id: String) : List<GenEntry> {
        val productionList = file.getList("$id.production") as List<List<Any>>

        val genList = productionList.map {
            val resultString = it[0] as String
            val result = resultString.getItemStack()
            val time = it[1] as Int

            val recipe = MachineRecipe(time, arrayOf(result), arrayOf(result))

            val energy = it[2] as Int
            GenEntry(recipe, energy)
        }

        return genList
    }
}