package de.westnordost.streetmeasure.Rendering

import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Material

class PolygonRenderer(private val scene: Scene, private val material: Material) {
    private var lineNodes = mutableListOf<Node>()

    fun render(anchors: List<Anchor>, planePose: Pose? = null) {
        // clear old
        lineNodes.forEach { it.setParent(null) }
        lineNodes.clear()
        if (anchors.size < 2) return

        for (i in anchors.indices) {
            val a = anchors[i].pose
            val b = anchors[(i + 1) % anchors.size].pose
            
            // Project to plane space if plane is provided
            val start = if (planePose != null) {
                projectToPlane(planePose, a)
            } else {
                Vector3(a.tx(), a.ty(), a.tz())
            }
            val end = if (planePose != null) {
                projectToPlane(planePose, b)
            } else {
                Vector3(b.tx(), b.ty(), b.tz())
            }
            
            // draw segment a->b as a thin cylinder or quad near plane
            val node = LineNode(material, start, end)
            node.setParent(scene)
            lineNodes += node
        }
    }
    
    private fun projectToPlane(planePose: Pose, worldPose: Pose): Vector3 {
        // Project world point to plane local space
        val localPoint = FloatArray(3)
        planePose.inverse().transformPoint(
            floatArrayOf(worldPose.tx(), worldPose.ty(), worldPose.tz()),
            0,
            localPoint,
            0
        )
        
        // Transform back to world space
        val worldResult = FloatArray(3)
        planePose.transformPoint(localPoint, 0, worldResult, 0)
        
        return Vector3(worldResult[0], worldResult[1], worldResult[2])
    }
}

