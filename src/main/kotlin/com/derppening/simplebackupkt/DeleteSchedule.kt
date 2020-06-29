package com.derppening.simplebackupkt

import java.util.*
import java.util.regex.Pattern

class DeleteSchedule(
        intervals: List<String>,
        frequencies: List<String>,
        private val backupFileManager: IBackupFileManager
) {

    private val intervals: List<DateModification>
    private val frequencies: List<DateModification?>

    init {
        val (i, f) = intervals.zip(frequencies)
                .map { (i, f) -> DateModification.fromString(i) to DateModification.fromString(f) }
                .filter { (i, _) -> i != null }
                .let { it.map { checkNotNull(it.first) } to it.map { it.second } }

        this.intervals = i
        this.frequencies = f
    }

    data class DateModification(
            val field: Int,
            val amount: Int
    ) {

        fun moveForward(cal: Calendar) {
            if (amount == 0) {
                throw UnsupportedOperationException()
            }
            cal.add(field, amount)
        }

        fun moveBack(cal: Calendar) {
            if (amount == 0) {
                throw UnsupportedOperationException()
            }
            cal.add(field, -amount)
        }

        companion object {
            fun fromString(s: String): DateModification? {
                if (s == "0") {
                    return DateModification(Calendar.DATE, 0)
                }

                Pattern.compile("(\\d+)([hdwmyHDWMY])").matcher(s)
                        .takeIf { it.matches() }
                        ?.let { it.group(1) to it.group(2) }
                        ?.let { (countStr, unitStr) ->
                            val unit = when (unitStr.toLowerCase()) {
                                "h" -> Calendar.HOUR
                                "d" -> Calendar.DATE
                                "w" -> Calendar.WEEK_OF_YEAR
                                "m" -> Calendar.MONTH
                                "y" -> Calendar.YEAR
                                else -> null
                            }

                            countStr.toIntOrNull() to unit
                        }
                        ?.takeIf { (count, unit) -> count != null && unit != null }
                        ?.let { (count, unit) -> DateModification(count!!, unit!!) }
                return null
            }
        }
    }

    fun deleteOldBackups() {
        if (intervals.isEmpty() || frequencies.isEmpty()) {
            return
        }

        val oldFiles = backupFileManager.backupList()
        if (oldFiles.isEmpty()) {
            return
        }

        var intervalEnd = Calendar.getInstance()
        intervalEnd.time = oldFiles.last()
        intervals[0].moveBack(intervalEnd)
        for (i in 1..intervals.size) {
            val intervalStart: Calendar?
            if (i < intervals.size) {
                intervalStart = intervalEnd.clone() as Calendar
                intervals[i].moveBack(intervalStart)
            } else {
                intervalStart = null
            }
            if (i <= frequencies.size && frequencies[i - 1] != null) {
                deleteExtraBackups(filter(oldFiles, intervalStart, intervalEnd), checkNotNull(frequencies[i - 1]))
            }
            intervalEnd = intervalStart
        }
    }

    private fun filter(oldFiles: SortedSet<Date>, from: Calendar?, to: Calendar): SortedSet<Date> {
        return TreeSet(oldFiles).apply {
            removeIf { from != null && it.before(from.time) || !it.before(to.time) }
        }
    }

    private fun deleteExtraBackups(files: SortedSet<Date>, desiredFrequency: DateModification) {
        if (files.isEmpty()) {
            return
        }
        var nextDate: Calendar? = null
        for (date in files) {
            when {
                desiredFrequency.amount == 0 -> {
                    backupFileManager.deleteBackup(date)
                }
                nextDate == null -> {
                    nextDate = Calendar.getInstance()
                    nextDate.time = date
                    desiredFrequency.moveForward(nextDate)
                }
                !date.before(nextDate.time) -> {
                    do {
                        desiredFrequency.moveForward(nextDate)
                    } while (!date.before(nextDate.time))
                }
                else -> {
                    backupFileManager.deleteBackup(date)
                }
            }
        }
    }
}