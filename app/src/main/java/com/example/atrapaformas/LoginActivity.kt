package com.example.atrapaformas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast // <-- Importante: Añadir la importación de Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    private var instructionsOverlay: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        val button_facil = findViewById<ImageButton>(R.id.button_facil)
        val button_medio = findViewById<ImageButton>(R.id.button_medio)
        val button_dificil = findViewById<ImageButton>(R.id.button_dificil)
        val nombreJugador = findViewById<EditText>(R.id.NombreJugador)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Mostrar overlay de instrucciones
        showInstructionsOverlay()

        button_facil.setOnClickListener {
            // 1. Obtener y "limpiar" el nombre
            val nombre = nombreJugador.text.toString().trim()

            // 2. Validar que no esté en blanco
            if (nombre.isNotBlank()) {
                // 3a. Si es válido: continuar
                val intent = Intent(this, JuegoFacilActivity::class.java)
                intent.putExtra("NOMBRE_JUGADOR", nombre)
                startActivity(intent)
            } else {
                // 3b. Si es inválido: mostrar Toast y no hacer nada más
                Toast.makeText(this, "¡Debes ingresar un nombre!", Toast.LENGTH_SHORT).show()
            }
        }

        button_medio.setOnClickListener {
            // Repetimos la misma lógica de validación
            val nombre = nombreJugador.text.toString().trim()

            if (nombre.isNotBlank()) {
                val intent = Intent(this, JuegoMedioActivity::class.java)
                intent.putExtra("NOMBRE_JUGADOR", nombre)
                startActivity(intent)
            } else {
                Toast.makeText(this, "¡Debes ingresar un nombre!", Toast.LENGTH_SHORT).show()
            }
        }

        button_dificil.setOnClickListener {
            // Repetimos la misma lógica de validación
            val nombre = nombreJugador.text.toString().trim()

            if (nombre.isNotBlank()) {
                val intent = Intent(this, JuegoDificilActivity::class.java)
                intent.putExtra("NOMBRE_JUGADOR", nombre)
                startActivity(intent)
            } else {
                Toast.makeText(this, "¡Debes ingresar un nombre!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ===============================
    // 4. OVERLAY DE INSTRUCCIONES
    // ===============================
    private fun showInstructionsOverlay() {
        // ... (Tu código de overlay existente, sin cambios) ...
        val rootView = findViewById<View>(android.R.id.content)
        val overlay = LayoutInflater.from(this)
            .inflate(R.layout.instructions_overlay, rootView as? ViewGroup, false)

        overlay.setOnClickListener {
            hideInstructionsOverlay()
        }

        (rootView as? ViewGroup)?.addView(overlay)
        instructionsOverlay = overlay
    }

    private fun hideInstructionsOverlay() {
        // ... (Tu código de overlay existente, sin cambios) ...
        instructionsOverlay?.let { overlay ->
            val rootView = findViewById<ViewGroup>(android.R.id.content)
            rootView.removeView(overlay)
            instructionsOverlay = null
        }
    }
}