package com.example.atrapaformas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Mostrar overlay de instrucciones
        showInstructionsOverlay()

        button_facil.setOnClickListener {
            val intent = Intent(this, JuegoFacilActivity::class.java)
            startActivity(intent)
        }
        button_medio.setOnClickListener {
            val intent = Intent(this, JuegoMedioActivity::class.java)
            startActivity(intent)
        }
        button_dificil.setOnClickListener {
            val intent = Intent(this, JuegoDificilActivity::class.java)
            startActivity(intent)
        }
    }

    // ===============================
    // 4. OVERLAY DE INSTRUCCIONES
    // ===============================
    private fun showInstructionsOverlay() {
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
        instructionsOverlay?.let { overlay ->
            val rootView = findViewById<ViewGroup>(android.R.id.content)
            rootView.removeView(overlay)
            instructionsOverlay = null
        }
    }
}