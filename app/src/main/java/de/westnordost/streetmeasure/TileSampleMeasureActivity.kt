package de.westnordost.streetmeasure

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.ar.core.*
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.rendering.Color
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import android.graphics.PointF

class TileSampleMeasureActivity : AppCompatActivity() {

    private lateinit var binding: de.westnordost.streetmeasure.databinding.ActivityTileSampleMeasureBinding
    private var arSceneView: ArSceneView? = null
    
    // AR session and state
    private var arSession: Session? = null
    private var isSessionCreated = false
    
    // Plane acquisition state
    private var activePlane: Plane? = null
    private var activePlanePose: Pose? = null   // plane's center pose
    private var activePlaneNormalY: Float? = null // plane's Y height for convenience
    
    // Trace overlay and measurement state
    private lateinit var traceOverlay: TileTraceOverlayView
    private lateinit var bottomPanel: android.widget.LinearLayout
    private lateinit var textResult: android.widget.TextView
    private lateinit var undoButton: android.widget.Button
    private lateinit var saveButton: android.widget.Button
    private lateinit var useButton: android.widget.Button
    
    // Measurement results
    private var lastTileWidthInches: Float = 0f
    private var lastTileHeightInches: Float = 0f
    private var lastTileAreaFt2: Float = 0f
    private var lastTileOutline2D: List<P2> = emptyList()
    
    // AR visualization
    private var tileRectangleNode: Node? = null
    
    companion object {
        const val EXTRA_TILE_WIDTH = "tile_width_inches"
        const val EXTRA_TILE_HEIGHT = "tile_height_inches"
        const val EXTRA_TILE_AREA = "tile_area_sqft"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize repositories
        TileSampleRepository.init(this)
        
        binding = de.westnordost.streetmeasure.databinding.ActivityTileSampleMeasureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        setupToolbar()
        setupViews()
        setupArScene()
        setupClickListeners()
        
        // Setup instruction popup
        binding.instructionPopup.setText("Trace around one tile with your finger.\nLift your finger to finish.")
        binding.instructionPopup.startFloatAnim()
        
        // Setup skip button
        binding.skipButton.setOnClickListener {
            val intent = Intent(this, TileCalculatorActivity::class.java)
            startActivity(intent)
            finish()
        }
        
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupViews() {
        traceOverlay = binding.traceOverlay
        bottomPanel = binding.bottomPanel
        textResult = binding.textResult
        undoButton = binding.undoButton
        saveButton = binding.saveButton
        useButton = binding.useButton
        
        // Wire up trace overlay callbacks
        traceOverlay.onStrokeStart = { startPoint ->
            captureReferencePlane(startPoint)
        }
        traceOverlay.onStrokeComplete = { strokePointsPx ->
            handleStrokeComplete(strokePointsPx)
        }
    }
    
    private fun disableTraceInput() {
        traceOverlay.isEnabled = false
        traceOverlay.isClickable = false
    }
    
    private fun enableTraceInput() {
        traceOverlay.isEnabled = true
        traceOverlay.isClickable = true
        traceOverlay.clearStroke()
    }
    
    private fun captureReferencePlane(startPoint: PointF) {
        if (!isSessionCreated || arSession == null) {
            Log.w("TileSampleMeasureActivity", "AR session not ready")
            return
        }
        
        try {
            val frame = arSession?.update()
            if (frame == null) {
                Log.w("TileSampleMeasureActivity", "AR frame not available")
                return
            }
            
            // Run hitTest at the start point
            val hits = frame.hitTest(startPoint.x, startPoint.y)
            
            // Find the first hit that is from any Plane (don't require specific type or tracking state yet)
            val planeHit = hits.firstOrNull { h ->
                val trackable = h.trackable
                trackable is Plane
            }
            
            if (planeHit != null) {
                val plane = planeHit.trackable as Plane
                val pose = planeHit.hitPose
                
                // Store the reference plane
                activePlane = plane
                activePlanePose = pose
                activePlaneNormalY = pose.ty()
                
                Log.d("TileSampleMeasureActivity", "Captured reference plane at Y=${activePlaneNormalY}")
            } else {
                // Fallback: synthesize a pseudo-plane using the camera pose
                val camPose = try {
                    frame.camera.displayOrientedPose
                } catch (e: Exception) {
                    frame.camera.pose
                }
                
                // Assume a horizontal plane at camera Y - 0.5 meters
                activePlane = null
                activePlanePose = camPose
                activePlaneNormalY = camPose.ty() - 0.5f
                
                Log.d("TileSampleMeasureActivity", "No plane found, using fallback at Y=${activePlaneNormalY}")
            }
            
        } catch (e: Exception) {
            Log.e("TileSampleMeasureActivity", "Error capturing reference plane", e)
            // Don't abort tracing - just use a fallback if possible
        }
    }
    
    private fun setupArScene() {
        // Initialize AR session
        lifecycleScope.launch {
            try {
                arSession = Session(this@TileSampleMeasureActivity)
                configureSession(arSession!!)
                isSessionCreated = true
                Log.d("TileSampleMeasureActivity", "AR session created successfully")
                
                // Create ArSceneView programmatically
                createArSceneView()
            } catch (e: Exception) {
                Log.e("TileSampleMeasureActivity", "Failed to create AR session", e)
                Toast.makeText(this@TileSampleMeasureActivity, "AR not supported on this device", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
    
    private fun configureSession(session: Session) {
        val config = Config(session)
        
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE // necessary for Sceneform
        config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
        // disabling unused features should make processing faster
        config.depthMode = Config.DepthMode.DISABLED
        config.cloudAnchorMode = Config.CloudAnchorMode.DISABLED
        config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
        config.lightEstimationMode = Config.LightEstimationMode.DISABLED
        config.flashMode = Config.FlashMode.OFF
        
        session.configure(config)
        Log.d("TileSampleMeasureActivity", "AR session configured successfully")
    }
    
    private fun createArSceneView() {
        try {
            val arSceneView = ArSceneView(this)
            arSceneView.planeRenderer.isEnabled = true
            binding.arSceneViewContainer.addView(arSceneView, android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT)
            
            // Only setup session if it's created
            if (arSession != null) {
                arSceneView.setupSession(arSession!!)
            }
            
                    // No longer need click listener - trace overlay handles touch
            this.arSceneView = arSceneView
            Log.d("TileSampleMeasureActivity", "ArSceneView created successfully")
        } catch (e: Exception) {
            Log.e("TileSampleMeasureActivity", "Failed to create ArSceneView", e)
            Toast.makeText(this, "Failed to initialize AR view", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    
        private fun setupClickListeners() {
        undoButton.setOnClickListener {
            resetTileCapture()
        }

        saveButton.setOnClickListener {
            showSaveOrContinueDialog()
        }

        useButton.setOnClickListener {
            useInCalculator()
        }
    }
    
    private fun handleStrokeComplete(strokePointsPx: List<PointF>) {
        // 1. Sanity check
        if (strokePointsPx.size < 10) {
            Toast.makeText(this, "Try again and trace fully around one tile while holding steady.", Toast.LENGTH_SHORT).show()
            // leave overlay stroke on screen so user sees what happened
            return
        }

        if (activePlaneNormalY == null || activePlanePose == null) {
            Toast.makeText(this, "Try again and trace fully around one tile while holding steady.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isSessionCreated || arSession == null) {
            Toast.makeText(this, "Try again and trace fully around one tile while holding steady.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val frame = arSession?.update()
            if (frame == null) {
                Toast.makeText(this, "Try again and trace fully around one tile while holding steady.", Toast.LENGTH_SHORT).show()
                return
            }

            val tracedWorldPts = mutableListOf<Vector3>()

            for (pt in strokePointsPx) {
                // Cast a ray from the camera through that screen point
                val camRay = arSceneView?.scene?.camera?.screenPointToRay(pt.x, pt.y)
                if (camRay == null) continue
                
                val origin = camRay.origin
                val dir = camRay.direction
                
                var worldPoint: Vector3? = null
                
                // Case A: we have a real ARCore Plane (activePlane != null)
                if (activePlane != null) {
                    val hits = frame.hitTest(pt.x, pt.y)
                    val samePlaneHit = hits.firstOrNull { h ->
                        val t = h.trackable
                        t is Plane && t == activePlane
                    }
                    if (samePlaneHit != null) {
                        val p = samePlaneHit.hitPose
                        worldPoint = Vector3(p.tx(), p.ty(), p.tz())
                    }
                }
                
                // Case B: fallback using the pseudo plane height
                if (worldPoint == null) {
                    val planeY = activePlaneNormalY
                    if (planeY != null) {
                        val denom = dir.y
                        if (kotlin.math.abs(denom) > 1e-5f) {
                            val t = (planeY - origin.y) / denom
                            if (t > 0f && t < 5f) {
                                val wx = origin.x + dir.x * t
                                val wy = planeY
                                val wz = origin.z + dir.z * t
                                worldPoint = Vector3(wx, wy, wz)
                            }
                        }
                    }
                }

                if (worldPoint != null) {
                    tracedWorldPts.add(worldPoint)
                }
            }

            if (tracedWorldPts.size < 10) {
                Toast.makeText(this, "Try again and trace fully around one tile while holding steady.", Toast.LENGTH_SHORT).show()
                return
            }

            // 3. Project to 2D plane coords (x,z)
            val pts2d = tracedWorldPts.map { P2(it.x, it.z) }

            // 4. Simplify polygon (basic downsample if we don't have Douglas-Peucker yet)
            val simplified = mutableListOf<P2>()
            val step = max(1, pts2d.size / 30) // keep ~30 pts
            for (i in pts2d.indices step step) {
                simplified.add(pts2d[i])
            }
            if (simplified.size < 3) {
                Toast.makeText(this, "Trace was too small or collapsed. Try again.", Toast.LENGTH_SHORT).show()
                return
            }

            // 5. Bounding box fallback
            val minX = simplified.minOf { it.x }
            val maxX = simplified.maxOf { it.x }
            val minZ = simplified.minOf { it.z }
            val maxZ = simplified.maxOf { it.z }

            val widthMeters = maxX - minX
            val heightMeters = maxZ - minZ

            if (widthMeters <= 0f || heightMeters <= 0f) {
                Toast.makeText(this, "Outline looks degenerate. Try again tracing the whole tile.", Toast.LENGTH_SHORT).show()
                return
            }

            val widthInches = widthMeters * 39.3701f
            val heightInches = heightMeters * 39.3701f
            val areaFt2 = (widthMeters * heightMeters) * 10.7639f

            val dispW = max(widthInches, heightInches)
            val dispH = min(widthInches, heightInches)

            val dispWround = ((dispW * 10f).roundToInt() / 10f)
            val dispHround = ((dispH * 10f).roundToInt() / 10f)
            val dispAround = ((areaFt2 * 100f).roundToInt() / 100f)

            // 6. Store these so Save / Use in Calculator can access
            lastTileWidthInches = dispWround
            lastTileHeightInches = dispHround
            lastTileAreaFt2 = dispAround
            lastTileOutline2D = simplified // keep this polygon for saving/preview

            // 7. Update AR overlay: draw a translucent quad from minX/minZ..maxX/maxZ on that plane Y
            // Use the activePlane if available, otherwise create a dummy plane for drawing
            val drawingPlane = activePlane ?: createFallbackPlaneForDrawing()
            drawMeasuredTileOverlay(minX, maxX, minZ, maxZ, drawingPlane)

            // 8. Fade out instruction popup
            binding.instructionPopup.fadeOut()
            
            // 9. Show bottom panel with results
            bottomPanel.visibility = android.view.View.VISIBLE
            textResult.text = "${dispWround} in x ${dispHround} in\n${dispAround} ft²"

            // Enable Save / Use / Undo buttons now
            saveButton.isEnabled = true
            useButton.isEnabled = true
            undoButton.isEnabled = true

            // 10. Disable further tracing until user hits Undo
            traceOverlay.isEnabled = false
            traceOverlay.isClickable = false

        } catch (e: Exception) {
            Log.e("TileSampleMeasureActivity", "Error processing stroke", e)
            Toast.makeText(this, "Error processing trace. Try again.", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun simplifyPolygon(points: List<P2>): List<P2> {
        // Simple decimation - take every 5th point to reduce noise
        // TODO: Implement proper Douglas-Peucker algorithm
        return points.filterIndexed { index, _ -> index % 5 == 0 }
    }
    
    private fun createFallbackPlaneForDrawing(): DummyPlane {
        // Create a simple fallback plane object for drawing
        return DummyPlane(activePlaneNormalY ?: 0f)
    }
    
    private data class DummyPlane(val y: Float)
    
    private fun drawMeasuredTileOverlay(minX: Float, maxX: Float, minZ: Float, maxZ: Float, plane: Any) {
        // Remove existing rectangle
        tileRectangleNode?.setParent(null)
        
        // Get the Y height from either a real Plane or our DummyPlane
        val planeY = when (plane) {
            is Plane -> plane.centerPose.ty()
            is DummyPlane -> plane.y
            else -> activePlaneNormalY ?: 0f
        }
        
        val center = Vector3((minX + maxX) / 2f, planeY, (minZ + maxZ) / 2f)
        val width = maxX - minX
        val height = maxZ - minZ
        
        val rectangleNode = Node()
        rectangleNode.worldPosition = center
        
        MaterialFactory.makeTransparentWithColor(this, Color(android.graphics.Color.parseColor("#40FFA500")))
            .thenAccept { material ->
                val rectangle = ShapeFactory.makeCube(Vector3(width, 0.002f, height), Vector3.zero(), material)
                rectangleNode.renderable = rectangle
                arSceneView?.scene?.addChild(rectangleNode)
                tileRectangleNode = rectangleNode
            }
    }
    
    private fun clearMeasuredTileOverlay() {
        tileRectangleNode?.setParent(null)
        tileRectangleNode = null
    }
    
    private fun performHitTest(x: Float, y: Float): HitResult? {
        return try {
            val frame = arSession?.update()
            val hitResults = frame?.hitTest(x, y)
            hitResults?.firstOrNull()
        } catch (e: Exception) {
            Log.e("TileSampleMeasureActivity", "Error during hit test", e)
            null
        }
    }
    
    
    private fun showSaveOrContinueDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Save this tile?")
            .setMessage("Would you like to save this tile sample before continuing?")
            .setPositiveButton("Save and Continue") { _, _ ->
                saveTileAndContinue()
            }
            .setNegativeButton("Continue Without Saving") { _, _ ->
                continueWithoutSaving()
            }
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun saveTileAndContinue() {
        val tileId = java.util.UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        
        val tileSample = TileSample(
            id = tileId,
            displayName = MeasurementUtils.formatDisplayName("Tile", timestamp),
            widthInInches = lastTileWidthInches,
            heightInInches = lastTileHeightInches,
            areaSqFt = lastTileAreaFt2,
            timestamp = timestamp,
            outlinePoints = lastTileOutline2D.map { TilePoint2D(it.x, it.z) }
        )
        
        TileSampleRepository.addTileSample(tileSample)
        Log.d("TileSampleMeasureActivity", "Saved tile sample: ${tileSample.displayName}")
        
        // Show completion dialog
        MaterialAlertDialogBuilder(this)
            .setTitle("Tile saved")
            .setMessage("Use this tile in the calculator now?")
            .setPositiveButton("Use in Calculator") { _, _ ->
                returnToCalculator()
            }
            .setNegativeButton("Done") { _, _ ->
                finish()
            }
            .show()
    }
    
    private fun continueWithoutSaving() {
        Log.d("TileSampleMeasureActivity", "Continuing without saving tile")
        // Return to calculator with dimensions
        returnToCalculator()
    }
    
    private fun returnToCalculator() {
        val resultIntent = Intent().apply {
            putExtra(EXTRA_TILE_WIDTH, lastTileWidthInches)
            putExtra(EXTRA_TILE_HEIGHT, lastTileHeightInches)
            putExtra(EXTRA_TILE_AREA, lastTileAreaFt2)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
    
    
    private fun showAdjustTileSizeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_adjust_tile_size, null)
        val editWidth = dialogView.findViewById<android.widget.EditText>(R.id.editWidth)
        val editHeight = dialogView.findViewById<android.widget.EditText>(R.id.editHeight)
        val buttonSnapCommonSize = dialogView.findViewById<android.widget.Button>(R.id.buttonSnapCommonSize)
        val buttonCancel = dialogView.findViewById<android.widget.Button>(R.id.buttonCancel)
        val buttonApply = dialogView.findViewById<android.widget.Button>(R.id.buttonApply)
        
        // Pre-fill with current values
        editWidth.setText(String.format("%.1f", lastTileWidthInches))
        editHeight.setText(String.format("%.1f", lastTileHeightInches))
        
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        buttonSnapCommonSize.setOnClickListener {
            showCommonSizeDialog(editWidth, editHeight)
        }
        
        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        buttonApply.setOnClickListener {
            val widthStr = editWidth.text.toString()
            val heightStr = editHeight.text.toString()
            
            try {
                val newWidth = widthStr.toFloat()
                val newHeight = heightStr.toFloat()
                
                if (newWidth <= 0f || newHeight <= 0f) {
                    Toast.makeText(this, "Please enter a valid size", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                // Update measurements
                lastTileWidthInches = newWidth
                lastTileHeightInches = newHeight
                lastTileAreaFt2 = (newWidth / 12f) * (newHeight / 12f) // convert to square feet
                
                // Update display
                val dispW = ((newWidth * 10f).roundToInt() / 10f)
                val dispH = ((newHeight * 10f).roundToInt() / 10f)
                val dispA = ((lastTileAreaFt2 * 100f).roundToInt() / 100f)
                
                textResult.text = "${dispW} in x ${dispH} in\n${dispA} ft²"
                
                dialog.dismiss()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            }
        }
        
        dialog.show()
    }
    
    private fun showCommonSizeDialog(editWidth: android.widget.EditText, editHeight: android.widget.EditText) {
        val commonSizes = arrayOf(
            "12 x 24 in",
            "12 x 12 in",
            "4 x 8 in",
            "4 x 6 in",
            "3 x 6 in"
        )
        
        android.app.AlertDialog.Builder(this)
            .setTitle("Select Common Size")
            .setItems(commonSizes) { _, which ->
                val size = when (which) {
                    0 -> Pair(12f, 24f)
                    1 -> Pair(12f, 12f)
                    2 -> Pair(4f, 8f)
                    3 -> Pair(4f, 6f)
                    4 -> Pair(3f, 6f)
                    else -> Pair(12f, 12f)
                }
                editWidth.setText(String.format("%.1f", size.first))
                editHeight.setText(String.format("%.1f", size.second))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun resetTileCapture() {
        // Hide bottom panel
        bottomPanel.visibility = android.view.View.GONE
        
        // Disable buttons
        saveButton.isEnabled = false
        useButton.isEnabled = false
        undoButton.isEnabled = false
        
        // Remove any AR quad overlay we drew
        clearMeasuredTileOverlay()
        
        // Clear stored measurements
        lastTileWidthInches = 0f
        lastTileHeightInches = 0f
        lastTileAreaFt2 = 0f
        lastTileOutline2D = emptyList()
        
        // Reset plane state so next trace can re-lock to a new tile
        activePlane = null
        activePlanePose = null
        activePlaneNormalY = null
        
        Log.d("TileSampleMeasureActivity", "Reset plane state for new trace")
        
        // Re-enable drawing and clear overlay stroke
        traceOverlay.isEnabled = true
        traceOverlay.isClickable = true
        traceOverlay.clearStrokeFromActivity()
        
        Log.d("TileSampleMeasureActivity", "Tile capture reset")
    }
    
    
    private fun useInCalculator() {
        returnToCalculator()
    }
    
    override fun onResume() {
        super.onResume()
        if (arSceneView != null && isSessionCreated) {
            try {
                arSceneView?.resume()
                Log.d("TileSampleMeasureActivity", "AR scene view resumed")
            } catch (e: Exception) {
                Log.e("TileSampleMeasureActivity", "Failed to resume AR scene view", e)
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        try {
            arSceneView?.pause()
            Log.d("TileSampleMeasureActivity", "AR scene view paused")
        } catch (e: Exception) {
            Log.e("TileSampleMeasureActivity", "Failed to pause AR scene view", e)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            arSceneView?.pause()
            arSceneView?.destroy()
            arSession?.close()
            Log.d("TileSampleMeasureActivity", "AR resources cleaned up")
        } catch (e: Exception) {
            Log.e("TileSampleMeasureActivity", "Failed to cleanup AR resources", e)
        }
    }
}

data class P2(val x: Float, val z: Float)
