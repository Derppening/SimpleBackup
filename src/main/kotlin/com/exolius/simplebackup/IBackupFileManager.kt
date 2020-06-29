package com.exolius.simplebackup

import java.io.File
import java.util.*

interface IBackupFileManager {
    fun createBackup(worldFolders: Iterable<File>): String
    fun backupList(): SortedSet<Date>
    fun deleteBackup(date: Date)
}