package com.example.kachate

// ----------------------------------------------------
// Modelos para la respuesta de la API de Open Food Facts
// ----------------------------------------------------

data class OffProductResponse(
    val status: Int, // 0 si no se encuentra, 1 si se encuentra
    val code: String?,
    val product: OffProduct?
)

data class OffProduct(
    val product_name: String?,
    val brands: String?,
    val ingredients_text: String?,
    val nutriments: OffNutriments?
)

data class OffNutriments(
    // Valores por 100g. Usamos String para evitar problemas de parsing si el dato está ausente.
    val energy_kcal_100g: String?,
    val fat_100g: String?,
    val saturated_fat_100g: String?,
    val carbohydrates_100g: String?,
    val sugars_100g: String?,
    val sodium_100g: String? // ¡Este valor está en GRAMOS!
)

// ----------------------------------------------------
// Función de extensión para mapear de OFF a NutritionData
// ----------------------------------------------------

fun OffProduct.toNutritionData(): NutritionData {
    // CORRECCIÓN: Se llama directamente a NutritionParser.analyzeIngredients.
    // Esto resuelve el error de referencia en OffModels.kt.
    val analyzedIngredients = ingredients_text?.let { NutritionParser.analyzeIngredients(it) } ?: emptyMap()

    return NutritionData(
        // 🚨 CRÍTICO: Usar TODOS los parámetros nombrados y en el ORDEN correcto (definido en NutritionData.kt)
        productName = product_name,
        brandName = brands,

        // Nutrientes (valores por 100g/ml)
        totalCalories = nutriments?.energy_kcal_100g,
        totalFat = nutriments?.fat_100g,
        saturatedFat = nutriments?.saturated_fat_100g,
        totalCarbohydrates = nutriments?.carbohydrates_100g,
        totalSugars = nutriments?.sugars_100g,

        // Conversión y mapeo de Sodio
        sodium = nutriments?.sodium_100g?.let {
            try {
                // Reemplaza coma por punto y multiplica por 1000 para pasar de g a mg
                (it.replace(",", ".").toDouble() * 1000).toInt().toString()
            } catch (e: Exception) {
                null
            }
        },

        // Ingredientes
        rawIngredientsText = ingredients_text,
        rawOcrText = null, // Viene de la cámara, no de la API, se deja null

        // Análisis de ingredientes
        saltStatus = analyzedIngredients["saltStatus"],
        oilStatus = analyzedIngredients["oilStatus"],
        sugarStatus = analyzedIngredients["sugarStatus"],
        vegetarianStatus = analyzedIngredients["vegetarianStatus"],
        veganStatus = analyzedIngredients["veganStatus"]
    )
}