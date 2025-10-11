package com.example.kachate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

// --- 1. Data Class para simular el Perfil del Usuario ---
// Normalmente esta data vendría de Firebase/Firestore o una API
data class UserProfile(
    val email: String,
    val dietType: String, // Ejemplo: "Vegano", "Keto", "Omnívoro"
    val allergies: String, // Ejemplo: "Gluten", "Lácteos", "Ninguna"
    val isPregnant: Boolean
)

class Recommendaciones : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recommendations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Simulación: Cargar el perfil del usuario (Esto se reemplazaría por una llamada real)
        val mockProfile = UserProfile(
            email = "ejemplo@usuario.com",
            dietType = "Vegano", // Cambia esto para probar: "Keto", "Vegetariano", "Omnívoro"
            allergies = "Gluten", // Cambia esto para probar: "Lácteos", "Ninguna"
            isPregnant = false
        )

        // 1. Mostrar resumen del perfil en el UI
        val tvProfileSummary = view.findViewById<TextView>(R.id.tvProfileSummary)
        tvProfileSummary.text = "Perfil: ${mockProfile.dietType} | Alergias: ${mockProfile.allergies}"

        // 2. Generar y mostrar recomendaciones
        val recommendations = getRecommendations(mockProfile)
        displayRecommendations(view, recommendations)
    }

    /**
     * Lógica principal para generar recomendaciones basadas en el perfil.
     * Aquí es donde se aplica la inteligencia y las restricciones.
     */
    private fun getRecommendations(profile: UserProfile): List<String> {
        val list = mutableListOf<String>()

        // 1. Recomendaciones basadas en el TIPO DE DIETA
        when (profile.dietType) {
            "Vegano" -> {
                list.add("🍔 Opción Principal: Hamburguesa de lentejas y champiñones con pan sin gluten.")
                list.add("🥗 Almuerzo Rápido: Ensalada de quinoa con garbanzos y aderezo de tahini.")
                list.add("🍰 Postre Vegano: Mousse de chocolate y aguacate con bayas.")
            }
            "Keto" -> {
                list.add("🥩 Opción Principal: Salmón al horno con espárragos y mantequilla de ajo.")
                list.add("🍳 Almuerzo Rápido: Huevos revueltos con queso crema y aguacate.")
                list.add("🥜 Snack Keto: Puñado de nueces de macadamia y pecanas.")
            }
            "Vegetariano" -> {
                list.add("🍝 Opción Principal: Pasta con pesto y tomates secos, coronada con queso parmesano.")
                list.add("🍲 Almuerzo Rápido: Sopa de calabaza y zanahoria con semillas de girasol.")
                list.add("🥚 Proteína: Omelette de espinacas y feta.")

            }
            else -> list.add("No se encontraron recomendaciones específicas para este tipo de dieta.")
        }

        // 2. Ajustes basados en ALERGIAS
        if (profile.allergies == "Gluten") {
            // Aplicar restricción de gluten a todas las recomendaciones
            list.add(0, "🛑 **ADVERTENCIA DE ALERGIA:** Asegúrate de que todos los panes, pastas y salsas sean etiquetados como 'Sin Gluten' (Gluten-Free).")
            // Reemplazar la recomendación principal si contenía pan/pasta
            if (profile.dietType == "Vegano") {
                list[1] = "🍔 Opción Principal (Ajustada): Hamburguesa de lentejas y champiñones servida en hoja de lechuga o con pan de maíz."
            }
        } else if (profile.allergies == "Lácteos") {
            list.add(0, "🛑 **ADVERTENCIA DE ALERGIA:** Reemplaza el queso, mantequilla, y leche por alternativas vegetales (almendra, soja, coco).")
        }

        // 3. Ajustes por CONDICIONES ESPECIALES (Embarazo)
        if (profile.isPregnant) {
            list.add(0, "✨ **NOTA POR EMBARAZO:** Prioriza alimentos ricos en hierro (legumbres, carnes rojas magras), ácido fólico (hojas verdes) y calcio.")
        }

        return list
    }

    /**
     * Crea dinámicamente TextViews para mostrar la lista de recomendaciones.
     */
    private fun displayRecommendations(view: View, recommendations: List<String>) {
        val container = view.findViewById<LinearLayout>(R.id.recommendationsContainer)
        container.removeAllViews() // Limpiar vistas anteriores

        recommendations.forEach { recommendation ->
            val textView = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 16 // Margen superior
                }
                text = recommendation
                textSize = 16f
                // Aplicar estilo basado en si es una advertencia
                if (recommendation.contains("ADVERTENCIA") || recommendation.contains("NOTA")) {
                    setTextColor(resources.getColor(R.color.purple_500, null)) // Usar el morado para avisos importantes
                    setTypeface(null, android.graphics.Typeface.BOLD)
                } else {
                    setTextColor(resources.getColor(android.R.color.black, null))
                }
            }
            container.addView(textView)
        }
    }
}
