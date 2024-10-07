package me.vaan.customitemgen

import io.github.seggan.sf4k.AbstractAddon
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class CustomItemGenerators : AbstractAddon() {

    companion object {
        private lateinit var _key: NamespacedKey
        private lateinit var _group: ItemGroup
        private lateinit var _instance: CustomItemGenerators

        val key: NamespacedKey
            get() = _key
        val group: ItemGroup
            get() = _group
        val instance: CustomItemGenerators
            get() = _instance
    }

    override suspend fun onEnableAsync() {
        val machines = genFile("machines.yml")
        _instance = this
        _key = NamespacedKey(this, "main_group")
        _group = ItemGroup(key, ItemStack(Material.GLOWSTONE))
        FileLoader.loadFiles(machines)
    }

    override suspend fun onDisableAsync() {

    }

    override fun getJavaPlugin(): JavaPlugin {
        return this
    }

    override fun getBugTrackerURL(): String {
        return "TODO"
    }

    private fun genFile(path: String): File {
        val file = File(this.dataFolder, path)
        if (!file.exists()) saveResource(path,false)
        return file
    }

}
