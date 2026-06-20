package se.familjenhed.minroj.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HighScore(
    val timeSeconds: Int,
    val date: Long
) {
    fun formattedTime(): String = "%02d:%02d".format(timeSeconds / 60, timeSeconds % 60)

    fun formattedDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(date))
}
