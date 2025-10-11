package com.example.kachate

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NutritionData(

    // 1. Información General del Producto (Normalmente de la API)
    val productName: String? = null,
    val brandName: String? = null,

    // 2. Información Nutricional (Valores por 100g)
    val totalCalories: String? = null, // en kcal
    val totalFat: String? = null,
    val saturatedFat: String? = null,
    val totalCarbohydrates: String? = null,
    val totalSugars: String? = null,
    val sodium: String? = null, // en mg

    // 3. Ingredientes (Texto crudo de la API o OCR)
    val rawIngredientsText: String? = null,
    val rawOcrText: String? = null, // Texto completo de la captura de cámara

    // 4. Análisis de Ingredientes (Calculado por NutritionParser)
    val saltStatus: String? = null,
    val oilStatus: String? = null,
    val sugarStatus: String? = null,

    // Perfiles Dietéticos
    val vegetarianStatus: String? = null,
    val veganStatus: String? = null

) : Parcelable