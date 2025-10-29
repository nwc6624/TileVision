package com.tilevision.util

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build

class TorchController(context: Context) {
    private val camMgr = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraIdWithFlash: String? = null
    var isTorchOn: Boolean = false
        private set

    init {
        // Pick a back-facing camera that supports flash
        for (id in camMgr.cameraIdList) {
            val chars = camMgr.getCameraCharacteristics(id)
            val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            val lensFacing = chars.get(CameraCharacteristics.LENS_FACING)
            val isBack = lensFacing == CameraCharacteristics.LENS_FACING_BACK
            if (hasFlash && isBack) {
                cameraIdWithFlash = id
                break
            }
        }
    }

    fun hasFlash(): Boolean = cameraIdWithFlash != null

    fun toggleTorch(on: Boolean) {
        val camId = cameraIdWithFlash ?: return
        try {
            camMgr.setTorchMode(camId, on)
            isTorchOn = on
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shutdown() {
        // Make sure we turn it off when leaving
        if (isTorchOn) {
            toggleTorch(false)
        }
    }
}
