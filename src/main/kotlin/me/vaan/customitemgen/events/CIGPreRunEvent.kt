package me.vaan.customitemgen.events

import me.vaan.customitemgen.data.SFMachine
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class CIGPreRunEvent(val machine: SFMachine, val validatorResult: Boolean) : Event(), Cancellable {

    private var cancelled = false

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

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(p0: Boolean) {
        cancelled = p0
    }
}