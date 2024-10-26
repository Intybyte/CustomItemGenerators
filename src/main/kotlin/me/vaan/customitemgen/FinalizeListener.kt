package me.vaan.customitemgen

import io.github.thebusybiscuit.slimefun4.api.events.SlimefunItemRegistryFinalizedEvent
import me.vaan.customitemgen.file.MachineLoader
import me.vaan.customitemgen.file.RecipeRegistry
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

object FinalizeListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun registerStuff(e: SlimefunItemRegistryFinalizedEvent) {
        val plugin = CustomItemGenerators.instance
        val machines = plugin.genFile("machines.yml")

        plugin.server.consoleSender.sendMessage("§aEnabling CustomItemGenerators!")
        RecipeRegistry.load()
        MachineLoader.load(machines)
    }
}