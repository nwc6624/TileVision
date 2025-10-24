package de.westnordost.streetmeasure

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingState.TRACKING
import com.google.ar.core.Plane.Type.HORIZONTAL_UPWARD_FACING
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ShapeFactory
import de.westnordost.streetmeasure.databinding.ActivityTileSampleMeasureBinding
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class TileSampleMeasureActivity : AppCompatActivity(), Scene.OnUpdateListener {

    private val createArCoreSession = ArCoreSessionCreator(this)
    private var initSessionOnResume = true

    private lateinit var binding: ActivityTileSampleMeasureBinding
    private var arSceneView: ArSceneView? = null

    private var pointRenderable: Renderable? = null
    private var lineRenderable: Renderable? = null

    // Tile measurement state
    private var firstPoint: Vector3? = null
    private var secondPoint: Vector3? = null
    private var firstAnchorNode: AnchorNode? = null
    private var secondAnchorNode: AnchorNode? = null
    private var lineNode: Node? = null

    private var isFirstTap = true
    private var hasCapturedPhoto = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        binding = ActivityTileSampleMeasureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Initialize repositories
        ProjectRepository.init(this)
        TileSampleRepository.init(this)

        setupUI()
        setupAR()
    }

    private fun setupUI() {
        // Set up click listeners
        binding.buttonSave.setOnClickListener {
            saveTileSample()
        }
        
        binding.buttonRetake.setOnClickListener {
            resetMeasurement()
        }
        
        binding.buttonBack.setOnClickListener {
            finish()
        }

        // Add tap listener to AR scene view for testing
        binding.arSceneView.setOnClickListener {
            if (isFirstTap) {
                simulateMeasurement()
                isFirstTap = false
            }
        }

        // Initially hide save button until we have measurements
        binding.buttonSave.isGone = true
        updateMeasurementDisplay()
    }

    private fun setupAR() {
        arSceneView = binding.arSceneView
        arSceneView?.scene?.addOnUpdateListener(this)

        // Create renderables
        createRenderables()
    }

    private fun createRenderables() {
        // Create point renderable
        MaterialFactory.makeOpaqueWithColor(this, Color(0.0f, 0.8f, 0.0f))
            .thenAccept { material ->
                pointRenderable = ShapeFactory.makeSphere(0.02f, Vector3.zero(), material)
            }

        // Create line renderable
        MaterialFactory.makeOpaqueWithColor(this, Color(0.0f, 0.8f, 0.0f))
            .thenAccept { material ->
                lineRenderable = ShapeFactory.makeCylinder(0.005f, 1.0f, Vector3.zero(), material)
            }
    }

    override fun onResume() {
        super.onResume()
        if (initSessionOnResume) {
            lifecycleScope.launch {
                initializeSession()
            }
        }
        arSceneView?.resume()
    }
    
    private suspend fun initializeSession() {
        initSessionOnResume = false
        val result = createArCoreSession()
        if (result is ArCoreSessionCreator.Success) {
            val session = result.session
            session.configure(Config(session).apply {
                planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
            })
            arSceneView?.setupSession(session)
        } else {
            Log.e("TileSampleMeasure", "Failed to setup AR session")
            Toast.makeText(this, "AR setup failed", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        arSceneView?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        arSceneView?.destroy()
    }

    override fun onUpdate(frameTime: FrameTime) {
        val frame = arSceneView?.arFrame ?: return
        val session = arSceneView?.session ?: return

        if (frame.camera.trackingState != TRACKING) {
            return
        }

        // TODO: Handle tap on screen - for now, this is a stub
        // In a real implementation, we would handle touch events and perform hit testing
    }

    private fun handleTap(hitResult: HitResult) {
        // TODO: Implement tile measurement logic
        // For now, just simulate a measurement
        simulateMeasurement()
    }
    
    private fun simulateMeasurement() {
        // Simulate measuring a 12x24 inch tile
        val widthInches = 12.0f
        val heightInches = 24.0f
        val areaFt2 = (widthInches * heightInches) / 144.0f
        
        updateMeasurementDisplay(widthInches, heightInches, areaFt2)
        binding.buttonSave.isVisible = true
        binding.instructionText.text = "Tap Save to store this tile sample"
        
        // Store the simulated values
        firstPoint = Vector3(0f, 0f, 0f)
        secondPoint = Vector3(0.3f, 0f, 0.6f) // Simulate 12x24 inch tile
    }

    private fun placeFirstPoint(hitResult: HitResult) {
        val anchor = hitResult.createAnchor()
        firstAnchorNode = AnchorNode(anchor).apply {
            renderable = pointRenderable
            arSceneView?.scene?.addChild(this)
        }
    }

    private fun placeSecondPoint(hitResult: HitResult) {
        val anchor = hitResult.createAnchor()
        secondAnchorNode = AnchorNode(anchor).apply {
            renderable = pointRenderable
            arSceneView?.scene?.addChild(this)
        }
    }

    private fun drawLine() {
        val first = firstPoint ?: return
        val second = secondPoint ?: return

        // Calculate line properties
        val direction = Vector3.subtract(second, first)
        val distance = direction.length()
        val center = Vector3.add(first, second).scaled(0.5f)
        val rotation = Quaternion.lookRotation(direction, Vector3.up())

        // Create line node
        lineNode = Node().apply {
            localPosition = center
            localRotation = rotation
            localScale = Vector3(1.0f, 1.0f, distance)
            renderable = lineRenderable
            arSceneView?.scene?.addChild(this)
        }
    }

    private fun calculateDimensions() {
        val first = firstPoint ?: return
        val second = secondPoint ?: return

        // Calculate distance between points
        val distance = Vector3.subtract(second, first).length()
        
        // For now, assume this is the width and calculate a reasonable height
        // TODO: Implement proper corner-to-corner measurement
        val widthInches = distance * 39.37f // Convert meters to inches
        val heightInches = widthInches * 0.5f // Placeholder - assume 2:1 ratio for now
        
        val areaFt2 = (widthInches * heightInches) / 144.0f // Convert to square feet

        updateMeasurementDisplay(widthInches, heightInches, areaFt2)
    }

    private fun updateMeasurementDisplay(width: Float = 0f, height: Float = 0f, area: Float = 0f) {
        if (width > 0 && height > 0) {
            binding.measurementText.text = "${String.format("%.1f", width)} in x ${String.format("%.1f", height)} in\n${String.format("%.2f", area)} ft²"
        } else {
            binding.measurementText.text = "Tap to measure tile"
        }
    }

    private fun capturePhotoFrame() {
        // TODO: Implement photo capture using ARCore camera image
        // For now, just mark as captured
        hasCapturedPhoto = true
        Log.d("TileSampleMeasure", "Photo capture stubbed - TODO: implement")
    }

    private fun saveTileSample() {
        val first = firstPoint ?: return
        val second = secondPoint ?: return
        
        val distance = Vector3.subtract(second, first).length()
        val widthInches = distance * 39.37f
        val heightInches = widthInches * 0.5f // Placeholder
        val areaFt2 = (widthInches * heightInches) / 144.0f

        // Show save/continue dialog
        showSaveContinueDialog(widthInches, heightInches, areaFt2)
    }
    
    private fun showSaveContinueDialog(width: Float, height: Float, areaFt2: Float) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Save this tile sample?")
            .setMessage("Would you like to save this tile before continuing?")
            .setPositiveButton("Save and Continue") { _, _ ->
                saveTileAndContinue(width, height, areaFt2)
            }
            .setNeutralButton("Continue Without Saving") { _, _ ->
                continueWithoutSaving(width, height, areaFt2)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
    
    private fun saveTileAndContinue(width: Float, height: Float, areaFt2: Float) {
        // Capture screenshot
        val screenshot = MeasurementUtils.captureArScreenshot(arSceneView!!)
        val screenshotPath = if (screenshot != null) {
            MeasurementUtils.saveScreenshot(this, screenshot, "tile_${System.currentTimeMillis()}")
        } else null
        
        // Create TileSample
        val timestamp = System.currentTimeMillis()
        val tileSample = TileSample(
            id = java.util.UUID.randomUUID().toString(),
            displayName = MeasurementUtils.formatDisplayName("Tile", timestamp),
            width = width,
            height = height,
            areaFt2 = areaFt2,
            units = "inches",
            timestamp = timestamp,
            previewImageUri = screenshotPath
        )
        
        // Save to repository
        TileSampleRepository.addTileSample(tileSample)
        
            Toast.makeText(this, "Tile sample saved!", Toast.LENGTH_SHORT).show()
            
            // Show the tile sample saved page first, then user can navigate to calculator
            val intent = Intent(this, TileSampleSavedActivity::class.java).apply {
                putExtra("tile_id", tileSample.id)
            }
            startActivity(intent)
            finish()
    }
    
    private fun continueWithoutSaving(width: Float, height: Float, areaFt2: Float) {
        // Return result to caller (TileCalculatorActivity)
        val resultIntent = Intent().apply {
            putExtra("tile_width", width)
            putExtra("tile_height", height)
            putExtra("tile_area", areaFt2)
            putExtra("tile_units", "inches")
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun resetMeasurement() {
        // Clear all nodes
        firstAnchorNode?.let { arSceneView?.scene?.removeChild(it) }
        secondAnchorNode?.let { arSceneView?.scene?.removeChild(it) }
        lineNode?.let { arSceneView?.scene?.removeChild(it) }

        // Reset state
        firstPoint = null
        secondPoint = null
        firstAnchorNode = null
        secondAnchorNode = null
        lineNode = null
        isFirstTap = true
        hasCapturedPhoto = false

        // Reset UI
        binding.buttonSave.isGone = true
        binding.instructionText.text = "Aim at one tile and tap to capture size + photo"
        updateMeasurementDisplay()
    }
}
