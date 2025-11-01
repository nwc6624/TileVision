package de.westnordost.streetmeasure.Rendering

import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.ShapeFactory
import kotlin.math.sqrt

class LineNode(private val material: Material, private val start: Vector3, private val end: Vector3) : Node() {
    init {
        val dx = end.x - start.x
        val dy = end.y - start.y
        val dz = end.z - start.z
        val length = sqrt(dx*dx + dy*dy + dz*dz)
        
        val cylinder = ShapeFactory.makeCylinder(
            0.0025f,
            length,
            Vector3.zero(),
            material
        )
        
        renderable = cylinder
        worldPosition = Vector3((start.x + end.x)/2f, (start.y + end.y)/2f, (start.z + end.z)/2f)
        worldRotation = Quaternion.lookRotation(Vector3(dx, dy, dz).normalized(), Vector3.up())
    }
}

