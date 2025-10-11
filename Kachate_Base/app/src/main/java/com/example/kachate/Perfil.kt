package com.example.kachate

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.kachate.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser

class Perfil : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupEditSpinners()
        setupGenderEditListener()
        loadProfileData()

        binding.buttonSaveChanges.setOnClickListener {
            saveProfileChanges()
        }

        binding.buttonDeleteAccount.setOnClickListener {
            confirmDeleteAccount()
        }

        return binding.root
    }

// ---------------------------------------------------------------------------------------------
// FUNCIONES DE CONFIGURACIÓN DE UI Y CARGA DE DATOS
// ---------------------------------------------------------------------------------------------

    private fun setupEditSpinners() {
        try {
            // Se asume que estos arrays existen en res/values/arrays.xml
            val allergiesAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.allergies_options, android.R.layout.simple_spinner_item)
            allergiesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerAllergiesEdit.adapter = allergiesAdapter

            val dietAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.diet_options, android.R.layout.simple_spinner_item)
            dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerDietTypeEdit.adapter = dietAdapter

            val genderAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.gender_options, android.R.layout.simple_spinner_item)
            genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerGenderEdit.adapter = genderAdapter

            val pregnancyAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.pregnancy_options, android.R.layout.simple_spinner_item)
            pregnancyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerPregnancyEdit.adapter = pregnancyAdapter

        } catch (e: Exception) {
            Toast.makeText(context, "Error: Faltan arrays de recursos. Revisar arrays.xml", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }


    private fun setupGenderEditListener() {
        binding.spinnerGenderEdit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedGender = parent?.getItemAtPosition(position).toString()

                if (selectedGender == "Mujer") {
                    binding.layoutPregnancyEdit.visibility = View.VISIBLE
                } else {
                    binding.layoutPregnancyEdit.visibility = View.GONE

                    val pregnancyAdapter = binding.spinnerPregnancyEdit.adapter as? ArrayAdapter<String>
                    if (pregnancyAdapter != null) {
                        val defaultPosition = pregnancyAdapter.getPosition("Selecciona").coerceAtLeast(0)
                        binding.spinnerPregnancyEdit.setSelection(defaultPosition)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    private fun loadProfileData() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(context, "No hay usuario autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        // El email se carga solo para mostrarlo (campo NO editable en XML)
        binding.editTextEmail.setText(user.email ?: "")

        db.collection("usuarios").document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                if (view == null || !document.exists()) return@addOnSuccessListener

                val allergies = document.getString("alergias")
                val dietType = document.getString("tipo_alimentacion")
                val gender = document.getString("genero")
                val pregnancy = document.getString("embarazo_lactancia")

                if (_binding != null) {
                    setSpinnerSelection(binding.spinnerAllergiesEdit, R.array.allergies_options, allergies)
                    setSpinnerSelection(binding.spinnerDietTypeEdit, R.array.diet_options, dietType)
                    setSpinnerSelection(binding.spinnerGenderEdit, R.array.gender_options, gender)

                    if (gender == "Mujer") {
                        binding.layoutPregnancyEdit.visibility = View.VISIBLE
                        setSpinnerSelection(binding.spinnerPregnancyEdit, R.array.pregnancy_options, pregnancy)
                    } else {
                        binding.layoutPregnancyEdit.visibility = View.GONE
                    }
                }
            }
            .addOnFailureListener {
                if (isAdded) {
                    Toast.makeText(context, "Error al cargar datos del perfil.", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun setSpinnerSelection(spinner: android.widget.Spinner, arrayResId: Int, value: String?) {
        if (value.isNullOrBlank()) return
        try {
            val resources = requireContext().resources
            val array = resources.getStringArray(arrayResId)
            val position = array.indexOf(value).coerceAtLeast(0)
            spinner.setSelection(position)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

// ---------------------------------------------------------------------------------------------
// LÓGICA DE GUARDAR CAMBIOS (SIMPLIFICADA)
// ---------------------------------------------------------------------------------------------

    private fun saveProfileChanges() {
        val user = auth.currentUser
        if (user == null) return

        // 1. Validar y recolectar datos de Spinners (Firestore)
        val newAllergies = binding.spinnerAllergiesEdit.selectedItem.toString()
        val newDietType = binding.spinnerDietTypeEdit.selectedItem.toString()
        val newGender = binding.spinnerGenderEdit.selectedItem.toString()

        val newPregnancy = if (newGender == "Mujer" && binding.layoutPregnancyEdit.visibility == View.VISIBLE) {
            val selected = binding.spinnerPregnancyEdit.selectedItem.toString()
            if (selected == "Selecciona") {
                Toast.makeText(context, "Debes seleccionar una opción de Embarazo/Lactancia.", Toast.LENGTH_SHORT).show()
                return
            }
            selected
        } else {
            "N/A"
        }

        if (newAllergies == "Selecciona" || newDietType == "Selecciona" || newGender == "Selecciona") {
            Toast.makeText(context, "Por favor, completa todas las preguntas.", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Guardar cambios en Firestore
        val updates = mapOf(
            "alergias" to newAllergies,
            "tipo_alimentacion" to newDietType,
            "genero" to newGender,
            "embarazo_lactancia" to newPregnancy
        )

        db.collection("usuarios").document(user.uid)
            .update(updates)
            .addOnSuccessListener {
                // 3. Si Firestore tiene éxito, proceder a actualizar solo Contraseña
                handlePasswordChange(user)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al actualizar perfil (Firestore): ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Gestiona únicamente el cambio de contraseña.
     */
    private fun handlePasswordChange(user: FirebaseUser) {
        // El email ya no es editable, solo nos importa el cambio de contraseña
        val currentPassword = binding.editTextCurrentPassword.text.toString()
        val newPassword = binding.editTextNewPassword.text.toString()

        // Si no hay nueva contraseña, terminamos con éxito.
        if (newPassword.isEmpty()) {
            Toast.makeText(context, "Perfil actualizado con éxito.", Toast.LENGTH_SHORT).show()
            return
        }

        // --- Validación de la Contraseña Actual ---
        // Se requiere la contraseña actual si la contraseña va a cambiar.
        if (currentPassword.isEmpty()) {
            Toast.makeText(context, "Se requiere la contraseña actual para cambiar la contraseña.", Toast.LENGTH_LONG).show()
            return
        }

        // 1. Re-autenticación requerida
        reauthenticateUser(user, currentPassword) { success ->
            if (success) {
                // Si la re-autenticación es exitosa, procedemos a cambiar la Contraseña
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Contraseña cambiada con éxito.", Toast.LENGTH_SHORT).show()
                        // Limpiar campos de contraseña tras éxito
                        binding.editTextCurrentPassword.setText("")
                        binding.editTextNewPassword.setText("")
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error al cambiar contraseña: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            // El mensaje de error de autenticación ya se maneja dentro de reauthenticateUser
        }
    }

    /**
     * Realiza la re-autenticación del usuario y llama a un callback.
     */
    private fun reauthenticateUser(user: FirebaseUser, currentPassword: String, onComplete: (Boolean) -> Unit) {
        // Solo re-autenticamos si tiene un email (no es una cuenta anónima)
        if (user.email == null) {
            onComplete(false)
            return
        }

        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error de autenticación: Contraseña actual incorrecta.", Toast.LENGTH_LONG).show()
                onComplete(false)
            }
    }

// ---------------------------------------------------------------------------------------------
// LÓGICA DE ELIMINACIÓN DE CUENTA
// ---------------------------------------------------------------------------------------------

    /** Muestra un diálogo de confirmación antes de eliminar la cuenta. */
    private fun confirmDeleteAccount() {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Cuenta")
            .setMessage("ADVERTENCIA: ¿Estás seguro de que quieres eliminar tu cuenta? Esta acción es irreversible y eliminará todos tus datos.")
            .setPositiveButton("Sí, Eliminar") { dialog, which ->
                deleteAccount()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /** Elimina el registro del usuario de Firestore y luego la cuenta de Firebase Auth. */
    private fun deleteAccount() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(context, "Error: No hay usuario para eliminar.", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Eliminar el registro del usuario en Firestore (Datos)
        db.collection("usuarios").document(user.uid)
            .delete()
            .addOnSuccessListener {
                // 2. Eliminar el usuario de Firebase Authentication
                user.delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Cuenta eliminada con éxito.", Toast.LENGTH_LONG).show()

                        // 3. Redirección al LoginActivity
                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error de seguridad. Por favor, inicia sesión de nuevo para eliminarla.", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al eliminar datos (Firestore).", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}