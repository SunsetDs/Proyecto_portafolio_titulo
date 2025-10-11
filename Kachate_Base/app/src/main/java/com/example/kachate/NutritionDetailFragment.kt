package com.example.kachate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment // <-- IMPORT CRÍTICO
import com.example.kachate.databinding.FragmentNutritionDetailBinding // <-- IMPORT CRÍTICO (Verifica el nombre)
import java.lang.IllegalStateException

class NutritionDetailFragment : Fragment() { // <-- Referencia 'Fragment' resuelta

    // Usamos View Binding para acceder a las vistas
    private var _binding: FragmentNutritionDetailBinding? = null // <-- Referencia resuelta
    private val binding get() = _binding!!

    // El objeto NutritionData que recibiremos
    private lateinit var nutritionData: NutritionData

    companion object {
        private const val ARG_NUTRITION_DATA = "nutrition_data"

        fun newInstance(data: NutritionData) = // <-- Referencia 'newInstance' resuelta en Camera.kt
            NutritionDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_NUTRITION_DATA, data)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // El nuevo método de Bundle usa el tipo de clase directamente (Kotlin/AndroidX)
        nutritionData = arguments?.getParcelable(ARG_NUTRITION_DATA)
            ?: throw IllegalStateException("NutritionData required for this fragment.")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNutritionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayNutritionData(nutritionData)
    }

    private fun displayNutritionData(data: NutritionData) {

        // --- 1. Información General (binding.tvNombre y otros resueltos) ---

        binding.tvNombre.text = data.productName ?: "Información no disponible"
        binding.tvMarca.text = data.brandName ?: "Información no disponible"

        val rawText = data.rawOcrText
        binding.tvPresentacion.text = if (rawText.isNullOrEmpty()) {
            "No se detectó texto OCR crudo."
        } else {
            rawText.take(300) + if (rawText.length > 300) "..." else ""
        }

        // --- 2. Información Nutricional (binding.tvEnergiaValue y otros resueltos) ---

        fun formatValue(value: String?, unit: String): String {
            return if (value.isNullOrBlank()) "N/D" else "$value $unit"
        }

        binding.tvEnergiaValue.text = formatValue(data.totalCalories, "kcal")
        binding.tvGrasasTotalesValue.text = formatValue(data.totalFat, "g")
        binding.tvGrasasSaturadasValue.text = formatValue(data.saturatedFat, "g")
        binding.tvCarbohidratosValue.text = formatValue(data.totalCarbohydrates, "g")
        binding.tvAzucaresValue.text = formatValue(data.totalSugars, "g")
        binding.tvSodioValue.text = formatValue(data.sodium, "mg")


        // --- 3. Análisis de Ingredientes (binding.tvSalInfo y otros resueltos) ---

        binding.tvSalInfo.text = data.saltStatus ?: "No se pudo determinar el contenido de sal/sodio."
        binding.tvAceiteInfo.text = data.oilStatus ?: "No se pudo determinar la presencia de aceites críticos."
        binding.tvGlucosaInfo.text = data.sugarStatus ?: "No se pudo determinar la presencia de azúcares añadidos."

        // Muestra ambos estatus en el mismo TextView (asumiendo que no agregaste tv_vegetarian_info)
        binding.tvVeganInfo.text = """
            Vegano: ${data.veganStatus ?: "No evaluado."}
            Vegetariano: ${data.vegetarianStatus ?: "No evaluado."}
        """.trimIndent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}