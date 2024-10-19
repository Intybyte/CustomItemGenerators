package me.vaan.customitemgen.file

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import me.vaan.customitemgen.CustomItemGenerators
import me.vaan.customitemgen.generator.ItemGenerator
import me.vaan.customitemgen.generator.Options
import me.vaan.customitemgen.util.getProduction
import me.vaan.customitemgen.util.getRecipe
import me.vaan.customitemgen.util.getStack
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.File

object MachineLoader {
    fun loadFiles(file: File) {
        val machines = YamlConfiguration()
        machines.load(file)

        for (id in machines.getKeys(false)) {
            if (!machines.getBoolean("$id.enabled")) {
                continue
            }

            val stack = machines.getStack("$id.item")
            val machineItem = SlimefunItemStack(id, stack)

            val energyCapacity = machines.getInt("$id.energy-capacity")

            val progressBarString = machines.getString("$id.progress-bar") ?: throw RuntimeException("$id machine progress bar entry not found")
            val progressBarMaterial = Material.getMaterial(progressBarString) ?: throw RuntimeException("$id machine progress bar material not found")

            val recipeTypeString = machines.getString("$id.recipe.type") ?: throw RuntimeException("$id machine recipe type not specified")
            val recipeType = RecipeRegistry[recipeTypeString] ?: throw RuntimeException("$id machine recipe type not found")

            val recipe = machines.getRecipe(id)
            if (recipe.isEmpty()) throw RuntimeException("$id has invalid/empty recipe")

            val production = machines.getProduction(id)
            if (production.isEmpty()) throw RuntimeException("$id has invalid/empty production list")

            val maxProduction = production.maxOf { it.energy }
            if (energyCapacity < maxProduction) throw RuntimeException("$id the energy-capacity must always be above the energy production cost")

            val entryRandomizer = machines.getBoolean("$id.entry-randomizer", false)

            val options = Options(
                entryRandomizer = entryRandomizer,
                progressBar = ItemStack(progressBarMaterial)
            )

            ItemGenerator(CustomItemGenerators.group, machineItem, recipeType, recipe, options, production)
                .setCapacity(energyCapacity)
                .register(CustomItemGenerators.instance)
        }
    }
}