package me.vaan.customitemgen.events

import me.vaan.customitemgen.data.SFMachine
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class CIGInitEvent(val machine: SFMachine, val position: Int, val consumption: Int) : Event() {

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }
}