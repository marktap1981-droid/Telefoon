package nl.voorraadbeheer.app.ui.scan

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

/**
 * CameraX ImageAnalysis.Analyzer die barcodes detecteert met ML Kit.
 * Roept [onBarcodeDetected] aan met de eerste geldige barcode-waarde die wordt gevonden.
 */
class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit,
) : androidx.camera.core.ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val value = barcodes.firstNotNullOfOrNull { it.rawValue }
                if (value != null) onBarcodeDetected(value)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
