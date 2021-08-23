package com.derppening.simplebackupkt

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.World
import org.bukkit.permissions.PermissionDefault
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.annotation.command.Command
import org.bukkit.plugin.java.annotation.permission.Permission
import org.bukkit.plugin.java.annotation.permission.Permissions
import org.bukkit.plugin.java.annotation.plugin.ApiVersion
import org.bukkit.plugin.java.annotation.plugin.Plugin
import java.io.File
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Level
import kotlin.math.floor

@Plugin(name = "SimpleBackupKt", version = "2.0")
@ApiVersion(ApiVersion.Target.v1_17)
@org.bukkit.plugin.java.annotation.command.Commands(Command(name = "sbackup", desc = "Run map backup", usage = "/sbackup"))
@Permissions(Permission(name = "simplebackup.use", desc = "Run map backup", defaultValue = PermissionDefault.OP))
class SimpleBackup : JavaPlugin() {

    private var interval: Double = 0.0
    private var startHour: Double? = null

    private var broadcast = true
    private var disableZipping = false
    private var backupEmpty = false
    private var selfPromotion = false

    private var message = "[SimpleBackup]"
    private var dateFormat = "yyyy-MM-dd-HH-mm-ss"
    private var backupFile = "backups/"
    private var customMessage = "Backup starting"
    private var customMessageEnd = "Backup completed"
    private var backupCommand = ""

    private lateinit var backupWorlds: List<String>
    private lateinit var additionalFolders: List<String>
    private lateinit var backupFileManager: IBackupFileManager
    private lateinit var deleteSchedule: DeleteSchedule

    private val loginListener = LoginListener()
    private val backupHooks = BackupHooks()

    /**
     * This is ran when the plugin is disabled
     */
    override fun onDisable() {
        server.scheduler.cancelTasks(this)
        logger.info("Disabled SimpleBackup")
    }

    /**
     * This is ran when the plugin is enabled
     */
    override fun onEnable() {
        // When plugin is enabled, load the "config.yml"
        loadConfiguration()

        if (!backupEmpty) {
            server.pluginManager.registerEvents(loginListener, this)
        }

        // Plugin commands
        getCommand("sbackup")?.setExecutor(Commands(this))

        // Shameless self promotion in the source code :D
        if (selfPromotion) {
            logger.info("Developed by Derppening, Based on SimpleBackup developed by Exolius")
        }

        // Set the backup interval, 72000.0D is 1 hour, multiplying it by the value interval will change the backup cycle time
        val ticks = (72_000 * this.interval).toLong()

        if (ticks > 0) {
            val delay = startHour?.let { syncStart(it) } ?: ticks
            // Add the repeating task, set it to repeat the specified time
            server.scheduler.runTaskTimerAsynchronously(this, Runnable {
                // When the task is run, start the map backup
                if (backupEmpty || Bukkit.getServer().onlinePlayers.isNotEmpty() || loginListener.wasOnline) {
                    doBackup()
                } else {
                    logger.info("Skipping backup (no one was online)")
                }
            }, delay, ticks)
            logger.info("Backup scheduled starting in ${delay / 72000.0} hours, repeat interval: $interval hours")
        }

        // After enabling, print to console to say if it was successful
        logger.info("Enabled.")
    }

    fun loadConfiguration() {
        // Set default values for variables
        interval = config.getDouble("backup-interval-hours")
        broadcast = config.getBoolean("broadcast-message")
        backupFile = requireNotNull(config.getString("backup-file"))
        backupWorlds = config.getStringList("backup-worlds")
        additionalFolders = config.getStringList("backup-folders")
        dateFormat = requireNotNull(config.getString("backup-date-format"))
        backupEmpty = config.getBoolean("backup-empty-server")
        message = requireNotNull(config.getString("backup-message"))
        customMessage = requireNotNull(config.getString("custom-backup-message"))
        customMessageEnd = requireNotNull(config.getString("custom-backup-message-end"))
        backupCommand = requireNotNull(config.getString("backup-completed-hook"))
        disableZipping = config.getBoolean("disable-zipping")
        selfPromotion = config.getBoolean("self-promotion")
        val startTime = config.getString("start-time")
        val intervalsStr = config.getStringList("delete-schedule.intervals")
        val frequenciesStr = config.getStringList("delete-schedule.interval-frequencies")

        // Save the configuration file
        config.options().copyDefaults(true)
        saveConfig()

        backupFileManager = if (disableZipping) {
            CopyBackup(backupFile, dateFormat, logger)
        } else {
            ZipBackup(backupFile, dateFormat, logger)
        }

        this.deleteSchedule = DeleteSchedule(intervalsStr, frequenciesStr, backupFileManager)
        val folders = foldersForBackup()
        val worlds = worldsForBackup()
        if (worlds.size < backupWorlds.size) {
            logger.warning("Not all listed worlds are recognized")
        }
        if (folders.size < additionalFolders.size) {
            logger.warning("Not all listed folders are recognized")
        }
        logger.info("Worlds $worlds scheduled for backup")
        if (folders.isNotEmpty()) {
            logger.info("Folders $folders scheduled for backup")
        }
        startTime?.also {
            try {
                val parsedTime = SimpleDateFormat("HH:mm").parse(it)
                startHour = hoursOf(parsedTime)
            } catch (ex: ParseException) {
                logger.warning("Can't parse time $it")
            }
        }
    }

    /**
     * This runs the map backup
     */
    @Synchronized
    fun doBackup() {
        // Begin backup of worlds
        // Broadcast the backup initialization if enabled
        if (broadcast) {
            server.scheduler.runTask(this, Runnable {
                server.broadcastMessage("${ChatColor.BLUE}$message $customMessage")
            })
        }

        // Loop through all the specified worlds and save them
        val foldersToBackup = worldsForBackup()
                .map {
                    it.isAutoSave = false
                    try {
                        server.scheduler.callSyncMethod(this) {
                            it.save()
                            null
                        }.get()
                    } catch (e: Exception) {
                        logger.log(Level.WARNING, e.message, e)
                    }
                    it.worldFolder.normalize()
                }
                .toMutableList()
                // additional folders, e.g. "plugins/"
                .apply { addAll(foldersForBackup()) }
                .toList()


        val backupFile = try {
            backupFileManager.createBackup(foldersToBackup)
        } catch (e: IOException) {
            logger.log(Level.WARNING, e.message, e)
            null
        }

        // re-enable auto-save
        worldsForBackup().forEach { it.isAutoSave = true }

        // delete old backups
        try {
            deleteSchedule.deleteOldBackups()
        } catch (e: IOException) {
            logger.log(Level.WARNING, e.message, e)
        }

        // Broadcast the backup completion if enabled
        if (broadcast) {
            server.scheduler.runTask(this, Runnable {
                server.broadcastMessage("${ChatColor.BLUE}$message $customMessageEnd")
            })
        }
        backupFile?.let {
            loginListener.notifyBackupCreated()
            backupHooks.notifyBackupCreated(backupCommand, backupFile)
        }
    }

    private fun foldersForBackup(): Collection<File> {
        return additionalFolders
                .map { File(".", it) }
                .filter { it.exists() }
    }

    private fun worldsForBackup(): Collection<World> {
        return server.worlds
                .filter { backupWorlds.isEmpty() || backupWorlds.contains(it.name) }
    }

    private fun hoursOf(parsedTime: Date): Double {
        val cal = Calendar.getInstance().also { it.time = parsedTime }
        return cal[Calendar.HOUR_OF_DAY] + cal[Calendar.MINUTE] / 60.0 + cal[Calendar.SECOND] / 3600.0
    }

    private fun syncStart(startHour: Double): Long {
        val now = hoursOf(Date())
        val diff = (now - startHour).let { if (it < 0) it + 24 else it }
        val intervalPart = diff - floor(diff / interval) * interval
        val remaining = interval - intervalPart
        return (remaining * 72000).toLong()
    }
}