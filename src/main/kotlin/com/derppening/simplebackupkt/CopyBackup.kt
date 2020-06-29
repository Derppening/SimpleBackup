package com.derppening.simplebackupkt

import org.apache.commons.io.FileUtils
import java.io.File
import java.util.*
import java.util.logging.Logger

class CopyBackup(backupFolder: String, fileNameDateFormat: String, logger: Logger) :
        BackupFileManager(backupFolder, fileNameDateFormat, logger) {

    override fun createBackup(worldFolders: Iterable<File>): String {
        val date = Date()
        val destination = File(backupFolder, getFileName(date))
        worldFolders.forEach {
            logger.info("Backing up $it")
            FileUtils.copyDirectory(it, File(destination, it.name))
        }
        return destination.absolutePath
    }

    override fun deleteBackup(date: Date) {
        val backupFile = File(backupFolder, getFileName(date))
        logger.info("Deleting backup ${backupFile.path}")
        backupFile.deleteRecursively()
    }

    override fun getFileName(date: Date): String = formatDate(date)
}