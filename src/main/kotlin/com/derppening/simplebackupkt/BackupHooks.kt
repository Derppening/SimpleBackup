package com.derppening.simplebackupkt

class BackupHooks {

    fun notifyBackupCreated(command: String, filename: String) {
        if (command.isNotEmpty()) {
            try {
                val pb = ProcessBuilder(command, filename)
                pb.start()
            } catch (ex: Exception) {
            }
        }
    }
}