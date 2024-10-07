package me.vaan.customitemgen

import io.github.seggan.sf4k.AbstractAddon
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import me.vaan.customitemgen.file.DisplayLoader
import me.vaan.customitemgen.file.MachineLoader
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class CustomItemGenerators : AbstractAddon() {

    companion object {
        private lateinit var _group: ItemGroup
        private lateinit var _instance: CustomItemGenerators

        val group: ItemGroup
            get() = _group
        val instance: CustomItemGenerators
            get() = _instance
    }

    override suspend fun onEnableAsync() {
        _instance = this
        val machines = genFile("machines.yml")
        saveDefaultConfig()

        val stack = config.getStack("GROUP.item")
        val key = NamespacedKey(this, "main_group")
        _group = ItemGroup(key, stack)

        DisplayLoader.loadFiles(config)
        //Load after every plugin has loaded
        Bukkit.getScheduler().runTaskLater(this, Runnable {
            MachineLoader.loadFiles(machines)
        }, 1L)
    }

    override suspend fun onDisableAsync() {

    }

    override fun getJavaPlugin(): JavaPlugin {
        return this
    }

    override fun getBugTrackerURL(): String {
        return "https://github.com/Intybyte/CustomItemGenerators/issues"
    }

    private fun genFile(path: String): File {
        val file = File(this.dataFolder, path)
        if (!file.exists()) saveResource(path,false)
        return file
    }
}
