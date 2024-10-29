package me.vaan.customitemgen.registry

import me.vaan.customitemgen.generator.ItemGenerator

object MachineRegistry : MutableMap<String, ItemGenerator> by HashMap()