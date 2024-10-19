package me.vaan.customitemgen.file

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import me.vaan.customitemgen.CustomItemGenerators
import me.vaan.customitemgen.generator.ItemGenerator
import me.vaan.customitemgen.generator.Options
import me.vaan.customitemgen.util.getBlock
import me.vaan.customitemgen.util.getProduction
import me.vaan.customitemgen.util.getRecipe
import org.bstats.charts.SimplePie
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.File

object MachineLoader {
    var registered: Set<SlimefunItem> = setOf()
        private set

    fun loadFiles(file: File) {
        val machines = YamlConfiguration()
        machines.load(file)

        val keys = machines.getKeys(false)
        val mutable = mutableListOf<SlimefunItem>()
        for (id in keys) {
            if (!machines.getBoolean("$id.enabled", true)) {
                continue
            }

            val stack = machines.getBlock("$id.item")
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

            val generator = ItemGenerator(CustomItemGenerators.group, machineItem, recipeType, recipe, options, production)
            generator
                .setCapacity(energyCapacity)
                .register(CustomItemGenerators.instance)

            generator.load() //have to post load it myself
            mutable.add(generator)
        }
        //in case this is called by some other addon (please don't call this)
        mutable.addAll(registered)
        registered = mutable.toSet()

        CustomItemGenerators.metrics.addCustomChart(
            SimplePie("custom_generators") {
                registered.size.toString()
            }
        )
    }
}