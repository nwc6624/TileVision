package com.tilevision.ar

import com.google.ar.core.Pose
import kotlin.math.abs
import kotlin.math.atan2

data class PolyCheck(val isValid: Boolean, val areaM2: Double)

object PolygonUtils {
    // Project 3D points to the detected plane and compute area (shoelace), after ordering.
    fun evaluate(planePose: Pose, worldPoints: List<FloatArray>): PolyCheck = validateAndArea(planePose, worldPoints)
    
    fun validateAndArea(planePose: Pose, worldPoints: List<FloatArray>): PolyCheck {
        if (worldPoints.size < 3) return PolyCheck(false, 0.0)

        // 1) project world points to plane local 2D
        val local2D = worldPoints.map { p ->
            val lp = FloatArray(3)
            planePose.inverse().transformPoint(p, 0, lp, 0)
            floatArrayOf(lp[0], lp[2]) // X & Z axes as 2D
        }

        // 2) order by polar angle around centroid to avoid self-intersections
        val cx = local2D.map { it[0] }.average()
        val cy = local2D.map { it[1] }.average()
        val ordered = local2D.sortedBy { atan2((it[1]-cy), (it[0]-cx)) }

        // 3) compute area via shoelace
        var sum = 0.0
        for (i in ordered.indices) {
            val (x1, y1) = ordered[i]
            val (x2, y2) = ordered[(i+1) % ordered.size]
            sum += (x1*y2 - x2*y1)
        }
        val area = abs(sum) * 0.5  // m^2 because points are in meters

        // 4) validity: >=3 verts AND area above epsilon (avoid tiny triangles)
        val valid = ordered.size >= 3 && area > 1e-4
        return PolyCheck(valid, area)
    }
}
