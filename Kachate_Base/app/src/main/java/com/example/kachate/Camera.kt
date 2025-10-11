package com.example.kachate
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kachate.databinding.FragmentCameraBinding
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class Camera : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraExecutor: ExecutorService
    private var imageAnalyzer: ImageAnalysis? = null

    private var scanningOcr = false
    private var scanningBarcode = false

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera()
            else Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) startCamera()
        else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.btnScanOcr.setOnClickListener {
            scanningOcr = true
            scanningBarcode = false
            binding.tvOcrResult.text = "Escaneando texto de etiqueta..."
            Toast.makeText(requireContext(), "Modo OCR activado", Toast.LENGTH_SHORT).show()
        }

        binding.btnScanApi.setOnClickListener {
            scanningBarcode = true
            scanningOcr = false
            binding.tvOcrResult.text = "Escaneando código de barras..."
            Toast.makeText(requireContext(), "Modo código de barras activado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImage(imageProxy)
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("CameraX", "Error al iniciar cámara", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        when {
            scanningOcr -> processOcr(image, imageProxy)
            scanningBarcode -> processBarcode(image, imageProxy)
            else -> imageProxy.close()
        }
    }

    private fun processOcr(image: InputImage, imageProxy: ImageProxy) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val detectedText = visionText.text.trim()
                if (detectedText.isNotEmpty()) {
                    binding.tvOcrResult.text = "Texto detectado:\n${cleanOcrText(detectedText)}"
                    scanningOcr = false
                    navigateToDetail(
                        "Producto detectado",
                        "Marca desconocida",
                        cleanOcrText(detectedText)
                    )
                } else {
                    binding.tvOcrResult.text = "No se detectó texto claro. Intenta de nuevo."
                }
            }
            .addOnFailureListener { e ->
                Log.e("OCR", "Error OCR", e)
                binding.tvOcrResult.text = "Error al leer texto"
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun processBarcode(image: InputImage, imageProxy: ImageProxy) {
        val scanner = BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { code ->
                        binding.tvOcrResult.text = "Código detectado: $code"
                        scanningBarcode = false
                        fetchNutritionFromOff(code)
                        break
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Barcode", "Error código de barras", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun fetchNutritionFromOff(barcode: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiUrl = "https://world.openfoodfacts.org/api/v0/product/$barcode.json"
                val json = URL(apiUrl).readText()
                val data = JSONObject(json)

                val product = data.optJSONObject("product")
                val productName = product?.optString("product_name", "Producto no encontrado")
                val brandName = product?.optString("brands", "Marca desconocida")
                val ingredients = product?.optString("ingredients_text", "Sin ingredientes disponibles")

                withContext(Dispatchers.Main) {
                    navigateToDetail(productName, brandName, ingredients)
                }
            } catch (e: Exception) {
                Log.e("API", "Error al obtener datos", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error al obtener datos del producto", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun cleanOcrText(text: String): String {
        return text
            .replace("\\s+".toRegex(), " ")
            .replace("[^A-Za-z0-9,.:;()%\\s]".toRegex(), "")
            .trim()
    }

    private fun navigateToDetail(nombre: String?, marca: String?, texto: String?) {
        val bundle = Bundle().apply {
            putString("nombre", nombre)
            putString("marca", marca)
            putString("texto", texto)
        }
        findNavController().navigate(R.id.action_camera_to_nutritionDetailFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }
}
