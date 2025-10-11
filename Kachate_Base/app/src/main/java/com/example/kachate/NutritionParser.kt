package com.example.kachate

object NutritionParser {

    // Regex para buscar patrones nutricionales: "Palabra clave", "Valor" (con o sin decimales)
    private val NUTRITION_REGEX = Regex(
        "(calorias|energia|fat|grasa|saturadas|carbohidratos|carbs|azucares|sodio)\\s*(\\d+[\\.,]?\\d*)"
    )

    // Keywords para identificar secciones de ingredientes.
    private val INGREDIENTS_KEYWORDS = listOf("ingredientes", "ingredients", "ingredients list")

    // --- Métodos de Ayuda ---

    /** Aísla la sección de ingredientes del texto OCR completo */
    private fun isolateIngredients(ocrText: String): String? {
        val lowerText = ocrText.lowercase()
        var startIndex = -1

        for (keyword in INGREDIENTS_KEYWORDS) {
            startIndex = lowerText.indexOf(keyword)
            if (startIndex != -1) {
                // Asume que los ingredientes vienen justo después de la palabra clave
                return ocrText.substring(startIndex + keyword.length).trim()
            }
        }
        return null
    }

    /**
     * Realiza un análisis detallado de los ingredientes para identificar aditivos y perfiles.
     * CRÍTICO: Debe ser 'internal' para ser accesible desde OffModels.kt
     */
    internal fun analyzeIngredients(ingredientsText: String): Map<String, String> {
        val results = mutableMapOf<String, String>()
        val text = ingredientsText.lowercase()

        // ------------------- 1. Azúcares Añadidos -------------------
        val sugarKeywords = listOf("azúcar", "jarabe", "sirope", "glucosa", "dextrosa", "fructosa", "maltodextrina")
        val foundSugars = sugarKeywords.filter { text.contains(it) }.toSet()
        results["sugarStatus"] = if (foundSugars.isNotEmpty()) {
            "Contiene azúcares añadidos: ${foundSugars.joinToString(", ")}"
        } else {
            "No se detectaron azúcares añadidos específicos."
        }

        // ------------------- 2. Sal/Sodio -------------------
        val saltKeywords = listOf("sal", "sodio", "cloruro de sodio", "monosódico")
        val foundSalts = saltKeywords.filter { text.contains(it) }.toSet()
        results["saltStatus"] = if (foundSalts.isNotEmpty()) {
            "Contiene sal/sodio: ${foundSalts.joinToString(", ")}"
        } else {
            "Bajo o sin sal/sodio añadido."
        }

        // ------------------- 3. Grasas/Aceites Críticos -------------------
        val oilKeywords = listOf("aceite de palma", "grasa vegetal", "grasa hidrogenada")
        val foundOils = oilKeywords.filter { text.contains(it) }.toSet()
        results["oilStatus"] = if (foundOils.isNotEmpty()) {
            "Contiene aceites críticos: ${foundOils.joinToString(", ")}"
        } else {
            "Aceites y grasas simples detectados."
        }

        // ------------------- 4. Perfiles Dietéticos -------------------

        // Ingredientes No Vegetarianos
        val nonVegetarianKeywords = listOf("gelatina", "carmín", "cochinilla", "caldo de carne", "grasa animal")
        val foundNonVeg = nonVegetarianKeywords.filter { text.contains(it) }.toSet()

        results["vegetarianStatus"] = if (foundNonVeg.isNotEmpty()) {
            "NO APTO (Contiene: ${foundNonVeg.joinToString(", ")})"
        } else {
            "APTO (No se detectaron ingredientes no vegetarianos comunes)."
        }

        // Ingredientes No Veganos (incluye leche y huevo)
        val nonVeganKeywords = listOf("leche", "huevo", "miel", "caseína", "lactosa", "suero") + nonVegetarianKeywords
        val foundNonVegan = nonVeganKeywords.filter { text.contains(it) }.toSet()

        results["veganStatus"] = if (foundNonVegan.isNotEmpty()) {
            "NO APTO (Contiene: ${foundNonVegan.joinToString(", ")})"
        } else {
            "APTO (No se detectaron ingredientes no veganos comunes)."
        }

        return results
    }

    // --- Función Principal de Parseo (Public para ser llamada desde Camera.kt) ---

    fun parse(ocrText: String): NutritionData {
        val results = mutableMapOf<String, String?>()
        val lowerText = ocrText.lowercase()

        // 1. Extraer los nutrientes usando la Regex
        NUTRITION_REGEX.findAll(lowerText).forEach { match ->
            val key = match.groupValues[1]
            val value = match.groupValues[2].replace(",", ".") // Normaliza decimales a punto

            when {
                key.contains("calorias") || key.contains("energia") -> results["totalCalories"] = value
                key == "fat" || key == "grasa" -> results["totalFat"] = value
                key.contains("saturadas") -> results["saturatedFat"] = value
                key == "carbohidratos" || key == "carbs" -> results["totalCarbohydrates"] = value
                key.contains("azucares") -> results["totalSugars"] = value
                key.contains("sodio") -> {
                    // El valor de sodio generalmente viene en mg
                    results["sodium"] = value
                }
            }
        }

        // 2. Aislar y analizar ingredientes
        val rawIngredientsText = isolateIngredients(ocrText)
        val ingredientAnalysis = rawIngredientsText?.let { analyzeIngredients(it) } ?: emptyMap()

        // 3. Construir NutritionData
        return NutritionData(
            // Nutrientes
            totalCalories = results["totalCalories"],
            totalFat = results["totalFat"],
            saturatedFat = results["saturatedFat"],
            totalCarbohydrates = results["totalCarbohydrates"],
            totalSugars = results["totalSugars"],
            sodium = results["sodium"],

            // Ingredientes
            rawIngredientsText = rawIngredientsText,

            // Análisis de ingredientes
            saltStatus = ingredientAnalysis["saltStatus"],
            oilStatus = ingredientAnalysis["oilStatus"],
            sugarStatus = ingredientAnalysis["sugarStatus"],
            vegetarianStatus = ingredientAnalysis["vegetarianStatus"], // Nuevo campo
            veganStatus = ingredientAnalysis["veganStatus"]
        )
    }
}