package com.tilevision.util

object Units {
    // Convert square meters to square feet
    // Using precise conversion constant: 1 m² = 10.76391041671 ft²
    fun m2ToSqFt(m2: Double): Double = m2 * 10.76391041671
    
    // Convert square feet to square meters
    fun sqFtToM2(sqFt: Double): Double = sqFt / 10.76391041671
    
    // Convert m² to UI display value based on unit preference
    fun m2ToUi(m2: Double, units: String): Pair<Double, String> {
        return if (units == "imperial") {
            Pair(m2ToSqFt(m2), "ft²")
        } else {
            Pair(m2, "m²")
        }
    }
}

