package com.derppening.simplebackupkt

import java.util.logging.Logger

class BackupHooks(private val logger: Logger) {

    fun notifyBackupCreated(command: String, filename: String) {
        if (command.isNotEmpty()) {
            runCatching {
                val pb = ProcessBuilder(command, filename)
                pb.start()
            }.onFailure { ex ->
                logger.warning("Failed to execute backup hook `$command $filename`: $ex")
            }
        }
    }
}