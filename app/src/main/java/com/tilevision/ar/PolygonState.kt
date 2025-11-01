package com.tilevision.ar

import com.google.ar.core.Plane
import com.google.ar.core.Pose

class PolygonState {
    private var planePose: Pose? = null
    private val pts = mutableListOf<FloatArray>()
    var valid = false; private set
    var areaM2 = 0.0; private set
    var ordered2D: List<Pair<Float, Float>> = emptyList(); private set

    fun setPlane(plane: Plane) { planePose = plane.centerPose }
    fun clear() { pts.clear(); valid=false; areaM2=0.0; ordered2D = emptyList() }
    fun add(world: FloatArray) {
        pts += world
        planePose?.let { 
            val r = PolygonUtils.evaluate(it, pts)
            valid = r.isValid
            areaM2 = r.areaM2
            ordered2D = r.ordered2D
        }
    }
    fun snapshot(): List<FloatArray> = pts.toList()
}

