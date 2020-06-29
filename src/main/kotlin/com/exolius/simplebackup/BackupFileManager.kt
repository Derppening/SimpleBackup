package com.exolius.simplebackup

import java.io.File
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Logger

abstract class BackupFileManager(
        protected val backupFolder: File,
        protected val fileNameDateFormat: SimpleDateFormat,
        protected val logger: Logger
) : IBackupFileManager {

    constructor(backupFolder: String, fileNameDateFormat: String, logger: Logger) : this(
            File(backupFolder),
            SimpleDateFormat(fileNameDateFormat),
            logger
    )

    protected fun formatDate(date: Date): String = fileNameDateFormat.format(date)

    override fun backupList(): SortedSet<Date> {
        val files = backupFolder.listFiles() ?: return TreeSet()
        val backups = files
                .map { it to fileNameDateFormat.parse(it.name, ParsePosition(0)) }
                .filter { (file, date) -> date != null && file.name == getFileName(date) }
                .map { it.second }
        return TreeSet<Date>().apply { addAll(backups) }
    }

    protected abstract fun getFileName(date: Date): String
}