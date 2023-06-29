package br.com.antoniomonteiro.checkdetector.ui.checkdetector

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import br.com.antoniomonteiro.checkdetector.R
import br.com.antoniomonteiro.checkdetector.databinding.FragmentCheckDetectorBinding
import com.google.android.odml.image.MediaMlImageBuilder
import com.google.android.odml.image.MlImage
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@ExperimentalGetImage class CheckDetectorFragment : Fragment() {

    private lateinit var imageCapture: ImageCapture
    private lateinit var viewModel: CheckDetectorViewModel

    private lateinit var binding: FragmentCheckDetectorBinding

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCheckDetectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[CheckDetectorViewModel::class.java]

        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, CheckAnalyser {mlImage ->
                        objectDetector?.let { objectDetect ->
                            objectDetect.process(mlImage)
                                .addOnSuccessListener {detectedObjects ->
                                    Log.i("Antonio", "Success")

                                    detectedObjects.firstOrNull()?.let { detectedObject ->
                                        detectedObject.labels.firstOrNull()?.let { label ->
                                            Log.i("AntonioLabel", label.text)
                                        }
                                    }

                                }
                                .addOnFailureListener {
                                    Log.i("Antonio", "Failure")
                                }
                        }
                    })
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer)

            } catch(exc: Exception) {
                Log.e("CheckDetectorCamera", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
        testOptions()
    }

    private lateinit var objectDetector : ObjectDetector

    private fun testOptions() {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableClassification()  // Optional
            .build()

        objectDetector = ObjectDetection.getClient(options)

    }


    @ExperimentalGetImage
    private class CheckAnalyser(val checkAnalyserCallback: (MlImage) -> Unit): ImageAnalysis.Analyzer {
        override fun analyze(image: ImageProxy) {
            val mlImage =
                MediaMlImageBuilder(image.image!!).setRotation(image.imageInfo.rotationDegrees).build()

            Log.i("Antonio","foi?")

            checkAnalyserCallback(mlImage)

            image.close() // quando finalizar o processo
        }

    }

}