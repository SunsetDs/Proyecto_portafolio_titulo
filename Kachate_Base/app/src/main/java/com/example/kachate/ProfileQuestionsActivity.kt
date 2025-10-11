package com.example.kachate

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kachate.databinding.ActivityProfileQuestionsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileQuestionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileQuestionsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileQuestionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupSpinners()
        setupGenderListener()

        binding.buttonContinue.setOnClickListener {
            saveUserData()
        }
    }


    private fun setupSpinners() {

        val allergiesAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.allergies_options,
            android.R.layout.simple_spinner_item
        )
        allergiesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAllergies.adapter = allergiesAdapter

        val dietAdapter = ArrayAdapter.createFromResource(this, R.array.diet_options, android.R.layout.simple_spinner_item)
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDietType.adapter = dietAdapter

        val genderAdapter = ArrayAdapter.createFromResource(this, R.array.gender_options, android.R.layout.simple_spinner_item)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGender.adapter = genderAdapter

        val pregnancyAdapter = ArrayAdapter.createFromResource(this, R.array.pregnancy_options, android.R.layout.simple_spinner_item)
        pregnancyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPregnancy.adapter = pregnancyAdapter
    }


    private fun setupGenderListener() {
        binding.spinnerGender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedGender = parent?.getItemAtPosition(position).toString()

                if (selectedGender == "Mujer") {
                    binding.layoutPregnancy.visibility = View.VISIBLE
                } else {
                    binding.layoutPregnancy.visibility = View.GONE

                    val pregnancyAdapter = binding.spinnerPregnancy.adapter as? ArrayAdapter<String>
                    val defaultPosition = pregnancyAdapter?.getPosition("Selecciona") ?: 0
                    binding.spinnerPregnancy.setSelection(defaultPosition)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }


    private fun saveUserData() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Error: Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        val allergies = binding.spinnerAllergies.selectedItem.toString()
        val dietType = binding.spinnerDietType.selectedItem.toString()
        val gender = binding.spinnerGender.selectedItem.toString()

        if (allergies == "Selecciona" || dietType == "Selecciona" || gender == "Selecciona") {
            Toast.makeText(this, "Por favor, completa todas las preguntas obligatorias.", Toast.LENGTH_LONG).show()
            return
        }

        val pregnancy: String

        if (gender == "Mujer" && binding.layoutPregnancy.visibility == View.VISIBLE) {
            val selectedPregnancy = binding.spinnerPregnancy.selectedItem.toString()

            if (selectedPregnancy == "Selecciona") {
                Toast.makeText(this, "Por favor, selecciona una opción de Embarazo/Lactancia.", Toast.LENGTH_SHORT).show()
                return
            }
            pregnancy = selectedPregnancy
        } else {
            pregnancy = "N/A"
        }

        val userProfile = hashMapOf(
            "alergias" to allergies,
            "tipo_alimentacion" to dietType,
            "genero" to gender,
            "embarazo_lactancia" to pregnancy,
        )

        db.collection("usuarios").document(user.uid)
            .set(userProfile)
            .addOnSuccessListener {
                Toast.makeText(this, "Perfil completado. ¡Bienvenido!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar perfil: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}