package com.example.atrapaformas

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat

class JuegoFacilActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_juego_facil)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setContentView(R.layout.activity_main)

        // Mostrar el overlay al iniciar
        showInstructionsOverlay()
    }
    private var instructionsOverlay: View? = null

    private fun showInstructionsOverlay() {
        // Obtener el contenedor root de la actividad
        val rootView = findViewById<ViewGroup>(android.R.id.content)

        // Inflar el layout del overlay
        val overlay = LayoutInflater.from(this)
            .inflate(R.layout.instructions_overlay, rootView, false)

        // Hacer que el overlay desaparezca al tocarlo
        overlay.setOnClickListener {
            hideInstructionsOverlay()
        }

        // Añadir el overlay encima de todo
        rootView.addView(overlay)
        instructionsOverlay = overlay
    }

    private fun hideInstructionsOverlay() {
        instructionsOverlay?.let { overlay ->
            val rootView = findViewById<ViewGroup>(android.R.id.content)
            rootView.removeView(overlay)
            instructionsOverlay = null
        }
    }
}
