package de.westnordost.streetmeasure.Rendering

import com.google.ar.core.Anchor
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Material

class PolygonRenderer(private val scene: Scene, private val material: Material) {
    private var lineNodes = mutableListOf<Node>()

    fun render(anchors: List<Anchor>) {
        // clear old
        lineNodes.forEach { it.setParent(null) }
        lineNodes.clear()
        if (anchors.size < 2) return

        for (i in anchors.indices) {
            val a = anchors[i].pose
            val b = anchors[(i + 1) % anchors.size].pose
            // draw segment a->b as a thin cylinder or quad near plane
            val node = LineNode(material, Vector3(a.tx(), a.ty(), a.tz()), Vector3(b.tx(), b.ty(), b.tz()))
            node.setParent(scene)
            lineNodes += node
        }
    }
}

