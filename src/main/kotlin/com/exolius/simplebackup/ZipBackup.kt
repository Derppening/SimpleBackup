package com.exolius.simplebackup

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI
import java.util.*
import java.util.logging.Logger
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipBackup(backupFolder: String, fileNameDateFormat: String, logger: Logger) :
        BackupFileManager(backupFolder, fileNameDateFormat, logger) {

    override fun createBackup(worldFolders: Iterable<File>): String {
        if (!backupFolder.exists()) {
            backupFolder.mkdirs()
        }
        val date = Date()
        val backupFile = File(backupFolder, getFileName(date))
        ZipOutputStream(FileOutputStream(backupFile)).use { zip ->
            worldFolders.forEach {
                logger.info("Backing up $it")
                zipFiles(it.absoluteFile.parentFile.toURI(), it, zip)
            }
        }
        return backupFile.absolutePath
    }

    override fun deleteBackup(date: Date) {
        val backupFile = File(backupFolder, getFileName(date))
        logger.info("Deleting backup ${backupFile.path}")
        backupFile.delete()
    }

    override fun getFileName(date: Date): String = "${formatDate(date)}.zip"

    private fun zipFiles(root: URI, source: File, zip: ZipOutputStream) {
        if (source.isDirectory) {
            val sources = checkNotNull(source.list()) { "Source directory does not exist" }
            sources.forEach { zipFiles(root, File(source, it), zip) }
        } else {
            val entry = ZipEntry(root.relativize(source.toURI()).path)
            zip.putNextEntry(entry)
            try {
                FileInputStream(source).buffered(4096).use {
                    it.copyTo(zip)
                }
            } catch (e: IOException) {
                logger.warning("Unable to backup file: ${source.absolutePath} (${e.message})")
            }
        }
    }
}