package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import android.content.Context
import android.graphics.Color
import android.util.Size
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun BarcodeScanner(
    modifier: Modifier = Modifier,
    isCameraFacingBack: Boolean,
    onScanSuccess: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val lensFacing =
        if(isCameraFacingBack) CameraSelector.LENS_FACING_BACK
        else CameraSelector.LENS_FACING_FRONT

    // Still confused on how CameraX and Barcode Scanner works
    // CameraX uses the View system, this line creates a PreviewView for the camera
    val previewView = remember { PreviewView(context) }

    // I think this selects the camera on your phone
    val cameraxSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        val preview = createPreview(previewView.display.rotation)
        val imageAnalysis = createImageAnalysis(
            previewView.display.rotation,
            onScanSuccess
        )

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, preview, imageAnalysis)
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(factory = {
            previewView.apply {
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                setBackgroundColor(Color.BLACK)
                scaleType = PreviewView.ScaleType.FILL_START
            }
        })
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }

private fun createPreview(
    rotation: Int
) : Preview {
    // This line creates the actual camera Preview you will see on the screen
    val preview = Preview.Builder()
        .setTargetRotation(rotation)
        .build()

    return preview
}

private fun createImageAnalysis(
    rotation: Int,
    onScanSuccess: (String) -> Unit,
) : ImageAnalysis {

    val imageResolution = Size(1280, 720)
    val resolutionStrategy = ResolutionStrategy(
        imageResolution,
        ResolutionStrategy.FALLBACK_RULE_NONE
    )
    val resolutionSelector = ResolutionSelector.Builder()
        .setResolutionStrategy(resolutionStrategy)
        .build()

    val imageAnalysis = ImageAnalysis.Builder()
        .setTargetRotation(rotation)
        .setResolutionSelector(resolutionSelector)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    val cameraExecutor = Executors.newSingleThreadExecutor()
    imageAnalysis.setAnalyzer(
        cameraExecutor,
        BarcodeAnalyzer { onScanSuccess(it.first().rawValue ?: "") }
    )

    return imageAnalysis
}