package com.exolius.simplebackup

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import kotlin.concurrent.thread

class Commands(private val plugin: SimpleBackup) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name.equals("sbackup", true)) {
            if (sender.hasPermission("simplebackup.use")) {
                thread { plugin.doBackup() }
            }
        }
        return false
    }
}