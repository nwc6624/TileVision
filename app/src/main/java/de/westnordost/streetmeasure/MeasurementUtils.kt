package de.westnordost.streetmeasure

import android.text.format.DateFormat
import java.util.Date

object MeasurementUtils {
    
    fun formatDisplayName(prefix: String, timestamp: Long): String {
        val date = Date(timestamp)
        val dateFormat = DateFormat.format("MMM dd, yyyy HH:mm", date)
        return "$prefix - $dateFormat"
    }
    
    fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val dateFormat = DateFormat.format("MMM dd, yyyy h:mm a", date)
        return dateFormat.toString()
    }
    
}
