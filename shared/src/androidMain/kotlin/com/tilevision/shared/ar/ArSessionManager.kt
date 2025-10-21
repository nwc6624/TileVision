package com.tilevision.shared.ar

import android.content.Context
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class ArSessionManager(private val context: Context) {
    private var session: Session? = null
    private var config: Config? = null
    
    private val _sessionState = MutableStateFlow(ArSessionState.UNINITIALIZED)
    actual val sessionState: StateFlow<ArSessionState> = _sessionState.asStateFlow()
    
    actual suspend fun startSession(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        try {
            if (!isArSupported()) {
                continuation.resumeWithException(UnavailableException(UnavailableException.UNSUPPORTED_DEVICE_NOT_CAPABLE))
                return@suspendCancellableCoroutine
            }
            
            _sessionState.value = ArSessionState.INITIALIZING
            
            session = Session(context)
            config = Config(session).apply {
                planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            }
            
            session?.configure(config)
            session?.resume()
            
            _sessionState.value = ArSessionState.RUNNING
            continuation.resume(Result.success(Unit))
            
        } catch (e: Exception) {
            _sessionState.value = ArSessionState.ERROR
            continuation.resumeWithException(e)
        }
    }
    
    actual suspend fun stopSession(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        try {
            session?.pause()
            session?.close()
            session = null
            config = null
            
            _sessionState.value = ArSessionState.STOPPED
            continuation.resume(Result.success(Unit))
            
        } catch (e: Exception) {
            _sessionState.value = ArSessionState.ERROR
            continuation.resumeWithException(e)
        }
    }
    
    actual suspend fun pauseSession(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        try {
            session?.pause()
            _sessionState.value = ArSessionState.PAUSED
            continuation.resume(Result.success(Unit))
            
        } catch (e: Exception) {
            _sessionState.value = ArSessionState.ERROR
            continuation.resumeWithException(e)
        }
    }
    
    actual suspend fun resumeSession(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        try {
            session?.resume()
            _sessionState.value = ArSessionState.RUNNING
            continuation.resume(Result.success(Unit))
            
        } catch (e: Exception) {
            _sessionState.value = ArSessionState.ERROR
            continuation.resumeWithException(e)
        }
    }
    
    actual suspend fun raycast(x: Float, y: Float): Result<ArRaycastResult?> = suspendCancellableCoroutine { continuation ->
        try {
            val currentSession = session ?: run {
                continuation.resume(Result.success(null))
                return@suspendCancellableCoroutine
            }
            
            val frame = currentSession.update()
            val hits = frame.hitTest(x, y)
            
            val result = hits.firstOrNull()?.let { hit ->
                val trackable = when (hit.trackable) {
                    is Plane -> ArTrackable.PLANE
                    is Point -> ArTrackable.POINT
                    is DepthPoint -> ArTrackable.DEPTH_POINT
                    else -> ArTrackable.POINT
                }
                
                val pose = hit.hitPose
                val poseData = ArPose(
                    position = ArVector3(pose.tx(), pose.ty(), pose.tz()),
                    rotation = ArQuaternion(pose.qx(), pose.qy(), pose.qz(), pose.qw())
                )
                
                ArRaycastResult(
                    hitPose = poseData,
                    distance = hit.distance,
                    trackable = trackable
                )
            }
            
            continuation.resume(Result.success(result))
            
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
    
    actual fun isArSupported(): Boolean {
        return try {
            val session = Session(context)
            session.close()
            true
        } catch (e: UnavailableException) {
            false
        }
    }
}
