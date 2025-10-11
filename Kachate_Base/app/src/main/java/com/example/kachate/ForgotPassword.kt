package com.example.kachate

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotpassword)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.editTextEmailReset)
        val sendButton = findViewById<Button>(R.id.buttonSendReset)
        val backToLoginTextView = findViewById<TextView>(R.id.textViewBackToLogin)


        sendButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            sendPasswordResetEmail(email)
        }

        backToLoginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        if (email.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa tu correo electrónico.", Toast.LENGTH_SHORT).show()
            return
        }

        val sendButton = findViewById<Button>(R.id.buttonSendReset)
        sendButton.isEnabled = false
        sendButton.text = "ENVIANDO..."

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                sendButton.isEnabled = true
                sendButton.text = "ENVIAR ENLACE"

                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "¡Listo! Se ha enviado un enlace a $email. Revisa tu bandeja de entrada o la carpeta de spam.",
                        Toast.LENGTH_LONG
                    ).show()

                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()

                } else {
                    Toast.makeText(
                        this,
                        "Error: Asegúrate que el correo es válido y está registrado.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}