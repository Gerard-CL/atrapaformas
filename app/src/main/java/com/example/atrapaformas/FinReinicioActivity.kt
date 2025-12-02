package com.example.atrapaformas

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class FinReinicioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fin_reinicio)

        // --- 1. Recuperar datos enviados desde la pantalla anterior ---
        val puntosRecibidos = intent.getIntExtra("PUNTUACION_FINAL", 0)
        // Recuperamos el nombre. Si no llega nada, ponemos una cadena vacía o "JUGADOR"
        val nombreRecibido = intent.getStringExtra("NOMBRE_JUGADOR") ?: ""

        // --- 2. Vincular las vistas del XML ---
        val botonJugar: ImageButton = findViewById(R.id.btn_play_again)
        val textViewPuntuacion: TextView = findViewById(R.id.puntuacion)
        // Buscamos el TextView del título que definiste en el XML
        val textViewTitulo: TextView = findViewById(R.id.titulo_sinvidas)

        // --- 3. Asignar los textos ---
        textViewPuntuacion.text = puntosRecibidos.toString()

        // AQUÍ ESTÁ EL CAMBIO: Cambiamos el texto del título
        // Usamos .uppercase() para que el nombre salga en mayúsculas como el resto del título
        // El \n hace un salto de línea para que quede estético
        textViewTitulo.text = "TE HAS QUEDADO SIN VIDAS\n${nombreRecibido.uppercase()}"


        // Configuración de bordes de pantalla (WindowInsets)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        botonJugar.setOnClickListener {
            // Cierra esta pantalla. Dependiendo de cómo tengas el flujo,
            // esto volverá al menú principal o cerrará la app.
            val intent = Intent(this, RankingActivity::class.java)

            // 2. Iniciamos la actividad
            startActivity(intent)
        }
    }
}