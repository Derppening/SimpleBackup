package com.derppening.simplebackupkt

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class LoginListener : Listener {

    var wasOnline = true
        private set

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        wasOnline = true
    }

    fun notifyBackupCreated() {
        wasOnline = false
    }
}