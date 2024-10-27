package me.vaan.customitemgen.data

import io.github.seggan.sf4k.serial.blockstorage.getBlockStorage
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.util.Vector

data class BlockRelative(
    val relative: Vector,
    val material: Material,
    val slimefunItem: String? = null
) {
    fun check(location: Location) : Boolean {
        // don't load the chunks
        if(!location.isChunkLoaded) return false

        val relativeLoc = location.clone().toBlockLocation().add(relative)

        val block = relativeLoc.block
        if (block.type != material) return false

        slimefunItem ?: return true

        val id = BlockStorage.getLocationInfo(relativeLoc, "id")
        return slimefunItem == id
    }
}
