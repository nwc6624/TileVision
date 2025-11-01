package com.tilevision.util

object Units {
    fun m2ToSqFt(m2: Double): Double = m2 * 10.76391041671
    
    fun m2ToUi(m2: Double, units: String): String =
        if (units == "imperial") String.format("%.2f sq ft", m2ToSqFt(m2))
        else String.format("%.3f m²", m2)
    
    // Legacy helper for backward compatibility
    fun sqFtToM2(sqFt: Double): Double = sqFt / 10.76391041671
}

