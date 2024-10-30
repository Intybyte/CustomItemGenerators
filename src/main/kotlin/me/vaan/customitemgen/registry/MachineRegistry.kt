package me.vaan.customitemgen.registry

import me.vaan.customitemgen.generator.ItemGenerator

/**
 * Map id to slimefun item generator
 */
object MachineRegistry : MutableMap<String, ItemGenerator> by HashMap()