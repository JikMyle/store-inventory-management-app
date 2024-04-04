package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage


class BarcodeAnalyzer(
    private val onScanSuccess: (List<Barcode>) -> Unit,
) : ImageAnalysis.Analyzer {
    private val tag = "BarcodeScanning"

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()
    private val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(options)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image

        if(mediaImage == null) {
            image.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)

        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if(barcodes.isNotEmpty()) {
                    onScanSuccess(barcodes)
                } else {
                    Log.d(tag, "$tag: No barcode found.")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(tag, "$tag: Something went wrong $exception")
            }
            .addOnCompleteListener {
                image.close()
            }
    }
}