package com.example.atrapaformas

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    private var instructionsOverlay: View? = null
    private lateinit var gestorPartidas: GestorPartidas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        mediaPlayer = MediaPlayer.create(this, R.raw.musicainicio)

// Configuramos para que se repita infinitamente
        mediaPlayer?.isLooping = true

// Arrancamos la música
        mediaPlayer?.start()

        gestorJugadores = GestorJugadores("${filesDir.path}/jugadores.json")
        // INICIALIZAR EL GESTOR DE PARTIDAS
        gestorPartidas = GestorPartidas("${filesDir.path}/partidas")

        val button_facil = findViewById<ImageButton>(R.id.button_facil)
        val button_medio = findViewById<ImageButton>(R.id.button_medio)
        val button_dificil = findViewById<ImageButton>(R.id.button_dificil)
        val nombreJugador = findViewById<EditText>(R.id.NombreJugador)
        val edadJugador = findViewById<EditText>(R.id.EdadJugador)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Mostrar overlay de instrucciones
        showInstructionsOverlay()

        // FUNCIÓN PARA VALIDAR DATOS (sin crear partida todavía)
        fun validarDatos(): Pair<String, Int>? {
            val nombre = nombreJugador.text.toString().trim()
            val edadTexto = edadJugador.text.toString().trim()

            if (nombre.isBlank()) {
                Toast.makeText(this, "¡Debes ingresar un nombre!", Toast.LENGTH_SHORT).show()
                return null
            }

            if (edadTexto.isBlank()) {
                Toast.makeText(this, "¡Debes ingresar tu edad!", Toast.LENGTH_SHORT).show()
                return null
            }

            val edad = edadTexto.toIntOrNull()
            if (edad == null || edad <= 0 || edad > 120) {
                Toast.makeText(this, "¡Edad no válida! (1-120)", Toast.LENGTH_SHORT).show()
                return null
            }

            return Pair(nombre, edad)
        }

        button_facil.setOnClickListener {
            val datos = validarDatos()
            if (datos != null) {
                val (nombre, edad) = datos
                // CREAR JUGADOR Y PARTIDA CON DIFICULTAD "Fácil"
                val jugador = Jugador.crearJugador(nombre, edad, "Fácil")
                val idPartida = gestorPartidas.crearPartida(jugador)

                val intent = Intent(this, JuegoFacilActivity::class.java)
                intent.putExtra("NOMBRE_JUGADOR", nombre)
                intent.putExtra("ID_PARTIDA", idPartida)
                startActivity(intent)
            }
        }

        button_medio.setOnClickListener {
            val datos = validarDatos()
            if (datos != null) {
                val (nombre, edad) = datos
                // CREAR JUGADOR Y PARTIDA CON DIFICULTAD "Medio"
                val jugador = Jugador.crearJugador(nombre, edad, "Medio")
                val idPartida = gestorPartidas.crearPartida(jugador)

                val intent = Intent(this, JuegoMedioActivity::class.java)
                intent.putExtra("NOMBRE_JUGADOR", nombre)
                intent.putExtra("ID_PARTIDA", idPartida)
                startActivity(intent)
            }
        }

        button_dificil.setOnClickListener {
            val datos = validarDatos()
            if (datos != null) {
                val (nombre, edad) = datos
                // CREAR JUGADOR Y PARTIDA CON DIFICULTAD "Difícil"
                val jugador = Jugador.crearJugador(nombre, edad, "Difícil")
                val idPartida = gestorPartidas.crearPartida(jugador)

                val intent = Intent(this, JuegoDificilActivity::class.java)
                intent.putExtra("NOMBRE_JUGADOR", nombre)
                intent.putExtra("ID_PARTIDA", idPartida)
                startActivity(intent)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Pausar si el usuario minimiza la app
        mediaPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        // Reanudar si el usuario vuelve
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Liberar memoria al cerrar la app completamente
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // ===============================
    // OVERLAY DE INSTRUCCIONES
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