package com.tilevision.util

object Units {
    // Convert square meters to square feet
    // Using precise conversion constant: 1 m² = 10.76391041671 ft²
    fun m2ToSqFt(m2: Double): Double = m2 * 10.76391041671
    
    // Convert square feet to square meters
    fun sqFtToM2(sqFt: Double): Double = sqFt / 10.76391041671
}

