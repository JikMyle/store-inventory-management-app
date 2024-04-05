package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import android.Manifest
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import androidx.compose.ui.geometry.Size as geometrySize
import androidx.compose.ui.graphics.Color as composeColor

@Composable
fun BarcodeScanner(
    modifier: Modifier = Modifier,
    hasCameraAccess: Boolean,
    isCameraFacingBack: Boolean,
    hasFoundBarcode: Boolean,
    onPermissionResult: (Boolean) -> Unit,
    onBarcodeScanned: (String) -> Unit,
) {
    val lensFacing =
        if (isCameraFacingBack) CameraSelector.LENS_FACING_BACK
        else CameraSelector.LENS_FACING_FRONT

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current

    val previewView = remember { PreviewView(context) }

    val cameraxSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val screenDensity = Resources.getSystem().displayMetrics.density

    val overlay =
        ScannerOverlay(
            256 * screenDensity,
            screenWidth.value * screenDensity,
            screenHeight.value * screenDensity
        )
//    val imageResolution = Size(1280, 720)

    val cameraPermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResult(isGranted)
    }

    LaunchedEffect(!hasCameraAccess) {
        cameraPermissionResultLauncher.launch(
            Manifest.permission.CAMERA
        )

        val cameraProvider = context.getCameraProvider()
        val preview = Preview.Builder().build()
        val imageAnalysis = createImageAnalysis(
            context,
//            imageResolution,
            overlay,
        ) {
            onBarcodeScanned(it)
        }

        Log.d("BarcodeAnalyzer", "Rotation in Compose: ${previewView.display.rotation}")

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, preview, imageAnalysis)
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    if (hasCameraAccess) {
        Box(modifier = modifier.fillMaxSize()) {
            AndroidView(factory = {
                previewView.apply {
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    setBackgroundColor(Color.BLACK)
                    scaleType = PreviewView.ScaleType.FILL_START
                }
            })

            Canvas(modifier = Modifier.fillMaxSize()) {
                drawPath(
                    path = overlay.path,
                    color = if(hasFoundBarcode) composeColor.Green else composeColor.Red,
                    style = Stroke(
                        width = 10.dp.value,
                        cap = StrokeCap.Round
                    )
                )
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(composeColor.Black)
        )
    }
}

class ScannerOverlay(
    size: Float,
    val viewWidth: Float,
    val viewHeight: Float
) {
    private val boundingBoxCornerOffset = (size / 4)

    private val canvasOffsetX = (viewWidth - size) / 2f
    private val canvasOffsetY = (viewHeight - size) / 2f

    val rect = Rect(
        Offset(canvasOffsetX, canvasOffsetY),
        geometrySize(size, size)
    )

    val path = Path().apply {
        moveTo(rect.topCenter.x - boundingBoxCornerOffset, rect.top)
        lineTo(rect.left, rect.top)
        lineTo(rect.left, rect.centerLeft.y - boundingBoxCornerOffset)

        moveTo(rect.left, rect.centerLeft.y + boundingBoxCornerOffset)
        lineTo(rect.left, rect.bottom)
        lineTo(rect.bottomCenter.x - boundingBoxCornerOffset, rect.bottom)

        moveTo(rect.bottomCenter.x + boundingBoxCornerOffset, rect.bottom)
        lineTo(rect.right, rect.bottom)
        lineTo(rect.right, rect.centerRight.y + boundingBoxCornerOffset)

        moveTo(rect.right, rect.centerRight.y - boundingBoxCornerOffset)
        lineTo(rect.right, rect.top)
        lineTo(rect.topCenter.x + boundingBoxCornerOffset, rect.top)
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

private fun createImageAnalysis(
    context: Context,
//    imageResolution: Size,
    overlay: ScannerOverlay,
    onBarcodeScanned: (String) -> Unit
): ImageAnalysis {

//    val resolutionStrategy = ResolutionStrategy(
//        imageResolution,
//        ResolutionStrategy.FALLBACK_RULE_NONE
//    )
//    val resolutionSelector = ResolutionSelector.Builder()
//        .setResolutionStrategy(resolutionStrategy)
//        .build()

    val imageAnalysis = ImageAnalysis.Builder()
//        .setResolutionSelector(resolutionSelector)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    imageAnalysis.setAnalyzer(
        ContextCompat.getMainExecutor(context),
        BarcodeAnalyzer(overlay) {
            onBarcodeScanned(it)
        }
    )

    return imageAnalysis
}