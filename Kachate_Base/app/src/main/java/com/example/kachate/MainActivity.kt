package com.example.kachate

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
// 💡 Asumo que tienes un archivo de layout llamado activity_main.xml
// y que contiene R.id.bottom_navigation y R.id.fragment_container

// 💡 Asumo que tienes clases Fragment llamadas HomeFragment, Camera, Perfil, y Recomendaciones
// Si Camera y Perfil son actividades, este código fallará. Asumo que son Fragments.
class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 🔑 Inicialización de Firebase Auth
        auth = FirebaseAuth.getInstance()

        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // 1. Cargar el Fragment de inicio si es la primera vez que se crea la actividad
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        // 2. Configurar el Listener para la barra de navegación inferior
        bottomNav.setOnItemSelectedListener { item ->
            // Se usa when para seleccionar y reemplazar el Fragment
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_camera -> Camera() // ⚠️ Asegúrate que 'Camera' es un Fragment
                R.id.nav_profile -> Perfil() // ⚠️ Asegúrate que 'Perfil' es un Fragment
                R.id.nav_recommendations -> Recommendaciones() // ⚠️ Asumo que el nombre es este
                else -> return@setOnItemSelectedListener false
            }
            replaceFragment(selectedFragment)
            true // Indica que el evento fue consumido
        }
    }

    /**
     * Reemplaza el Fragment actual en el contenedor.
     */
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            // 🔑 Usar un ID de contenedor genérico para el FrameLayout o FragmentContainerView
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // ----------------------------------------------------
    // Lógica de Menú de la Barra Superior (Logout)
    // ----------------------------------------------------

    /**
     * Infla el menú de la barra superior (asumo que 'menuclose' contiene action_logout)
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menuclose, menu) // 💡 R.menu.menuclose debe existir
        return true
    }

    /**
     * Maneja el clic en el ítem de menú.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                signOutUser()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Cierra la sesión del usuario de Firebase y navega a la pantalla de Login.
     */
    private fun signOutUser() {
        auth.signOut()
        Toast.makeText(this, "Sesión cerrada.", Toast.LENGTH_SHORT).show()

        // Iniciar la actividad de Login y limpiar la pila de actividades
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Finaliza MainActivity
    }
}