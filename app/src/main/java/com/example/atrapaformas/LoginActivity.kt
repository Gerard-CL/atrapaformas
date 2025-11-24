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
    private var mediaPlayer: MediaPlayer? = null

    // Vistas
    private lateinit var nombreJugador: EditText
    private lateinit var edadJugador: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // 1. Configurar Música (Loop)
        mediaPlayer = MediaPlayer.create(this, R.raw.musicainicio).apply {
            isLooping = true
            start()
        }

        // 2. Inicializar el Gestor de Partidas
        // Usamos absolutePath para asegurar la ruta correcta en Android
        gestorPartidas = GestorPartidas("${filesDir.absolutePath}/partidas")

        // 3. Referencias a Vistas
        val buttonFacil = findViewById<ImageButton>(R.id.button_facil)
        val buttonMedio = findViewById<ImageButton>(R.id.button_medio)
        val buttonDificil = findViewById<ImageButton>(R.id.button_dificil)
        nombreJugador = findViewById(R.id.NombreJugador)
        edadJugador = findViewById(R.id.EdadJugador)

        // Ajuste de márgenes para bordes del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 4. Mostrar instrucciones al inicio
        showInstructionsOverlay()

        // 5. Configurar Listeners de los botones
        buttonFacil.setOnClickListener {
            procesarInicioPartida("Fácil", JuegoFacilActivity::class.java)
        }

        buttonMedio.setOnClickListener {
            procesarInicioPartida("Medio", JuegoMedioActivity::class.java)
        }

        buttonDificil.setOnClickListener {
            procesarInicioPartida("Difícil", JuegoDificilActivity::class.java)
        }
    }

    /**
     * Función auxiliar para validar datos, crear jugador, guardar partida y navegar.
     * Evita repetir código 3 veces.
     */
    private fun procesarInicioPartida(dificultad: String, activityDestino: Class<*>) {
        val datos = validarDatos()

        if (datos != null) {
            val (nombre, edad) = datos

            // --- CAMBIO PRINCIPAL ---
            // Usamos el constructor directo. Las fechas se ponen solas gracias a Utils.
            val jugador = Jugador(
                nombre = nombre,
                edad = edad,
                dificultad = dificultad
                                 )

            // Guardamos partida
            val idPartida = gestorPartidas.crearPartida(jugador)

            // Navegamos
            val intent = Intent(this, activityDestino)
            intent.putExtra("NOMBRE_JUGADOR", nombre)
            intent.putExtra("ID_PARTIDA", idPartida)
            startActivity(intent)
        }
    }

    private fun validarDatos(): Pair<String, Int>? {
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

    // ===============================
    // CICLO DE VIDA (Música)
    // ===============================
    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // ===============================
    // OVERLAY DE INSTRUCCIONES
    // ===============================
    private fun showInstructionsOverlay() {
        val rootView = findViewById<ViewGroup>(android.R.id.content)
        // Usamos try-catch o verificamos layout por seguridad, pero aquí asumo que existe
        val overlay = LayoutInflater.from(this)
            .inflate(R.layout.instructions_overlay, rootView, false)

        overlay.setOnClickListener {
            hideInstructionsOverlay()
        }

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