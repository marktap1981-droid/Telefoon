package nl.voorraadbeheer.app.util

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val displayFormat = SimpleDateFormat("d MMM yyyy", Locale("nl", "NL"))

fun Timestamp.toDisplayString(): String = displayFormat.format(this.toDate())

fun Timestamp.daysUntil(): Long {
    val now = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val target = Calendar.getInstance().apply {
        time = this@daysUntil.toDate()
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val diffMillis = target.timeInMillis - now.timeInMillis
    return diffMillis / (24 * 60 * 60 * 1000)
}

fun dateFromMillis(millis: Long): Timestamp = Timestamp(Date(millis))
