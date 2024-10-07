package me.vaan.customitemgen.generator

import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe

data class GenEntry(
    val recipe: MachineRecipe,
    val energy: Int
)
