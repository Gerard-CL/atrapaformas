package com.example.atrapaformas

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog // Necesario para la ventana emergente
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.Random

class JuegoDificilActivity : AppCompatActivity() {

    // --- 1. Variables del Juego ---
    private var vidas = 3
    private var puntos = 0
    private var record = 0
    private var isJuegoActivo = true

    // --- NUEVO: Variable de estado de pausa
    private var isPausado = false

    private lateinit var tvVidas: TextView
    private lateinit var tvPuntos: TextView
    private lateinit var ivTargetShape: ImageView
    private lateinit var cieloContainer: ConstraintLayout

    // --- NUEVO: Botón de pausa
    private lateinit var btnPause: View

    private var currentTargetDrawableId: Int = 0

    private val gameHandler = Handler(Looper.getMainLooper())
    private val random = Random()

    private var mediaPlayer: MediaPlayer? = null

    // --- NUEVO: Lista para controlar animaciones activas
    private val animadoresActivos = mutableListOf<ObjectAnimator>()

    // --- NUEVO: Runnable global
    private lateinit var gameLoop: Runnable

    private val imagenesJuego = listOf(
        R.drawable.cuadrado_formas,
        R.drawable.triangulos_formas,
        R.drawable.rombos_formas,
        R.drawable.circulos_formas
                                      )

    // --- 2. Método Principal ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // OJO: Estás usando el layout de 'juego_facil', asegúrate que tiene el botón de pausa
        setContentView(R.layout.activity_juego_facil)

        mediaPlayer = MediaPlayer.create(this, R.raw.fondomusica1)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        val nombreRecibido = intent.getStringExtra("NOMBRE_JUGADOR")
        val textViewNombre = findViewById<TextView>(R.id.tv_usuario)
        textViewNombre.text = nombreRecibido ?: "Jugador"

        // Conectar vistas
        tvVidas = findViewById(R.id.tv_vidas)
        tvPuntos = findViewById(R.id.tv_puntos)
        ivTargetShape = findViewById(R.id.iv_target_shape)
        cieloContainer = findViewById(R.id.cielo_container)

        // --- NUEVO: Configurar botón de pausa
        btnPause = findViewById(R.id.button_pause)
        btnPause.setOnClickListener {
            mostrarDialogoPausa()
        }

        tvVidas.text = "VIDAS: $vidas"
        tvPuntos.text = "PUNTOS: $puntos"

        record = 112

        // Definimos el loop y arrancamos
        definirGameLoop()
        iniciarJuego()
    }

    // --- NUEVO: Lógica del Menú de Pausa ---
    private fun mostrarDialogoPausa() {
        if (!isJuegoActivo) return

        pausarLogicaJuego()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pausa")
        builder.setMessage("¿Qué quieres hacer?")
        builder.setCancelable(false)

        builder.setPositiveButton("Reanudar") { dialog, _ ->
            reanudarLogicaJuego()
            dialog.dismiss()
        }

        builder.setNegativeButton("Salir") { dialog, _ ->
            finish()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun pausarLogicaJuego() {
        isPausado = true
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
        // Detener generación
        gameHandler.removeCallbacks(gameLoop)
        gameHandler.removeCallbacksAndMessages(null)

        // Congelar figuras cayendo
        for (anim in animadoresActivos) {
            if (anim.isRunning) {
                anim.pause()
            }
        }
    }

    private fun reanudarLogicaJuego() {
        isPausado = false
        mediaPlayer?.start()

        // Reanudar figuras
        for (anim in animadoresActivos) {
            if (anim.isPaused) {
                anim.resume()
            }
        }
        // Reanudar generación
        gameHandler.post(gameLoop)
    }
    class JuegoDificilActivity : AppCompatActivity() {
        private lateinit var gestorPartidas: GestorPartidas
        private var idPartida: String = ""

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_juego_facil)

            gestorPartidas = GestorPartidas("${filesDir.path}/partidas")
            idPartida = intent.getStringExtra("ID_PARTIDA") ?: ""

            // SABEMOS que es dificultad "Fácil" porque estamos en JuegoFacilActivity
            // ... tu código del juego fácil
        }

        fun cuandoTermineElJuego(puntuacion: Int, tiempoJugado: Int) {
            gestorPartidas.finalizarPartida(idPartida, puntuacion, tiempoJugado)

            val intent = Intent(this, FinReinicioActivity::class.java)
            intent.putExtra("PUNTUACION", puntuacion)
            intent.putExtra("ID_PARTIDA", idPartida)
            intent.putExtra("DIFICULTAD", "Difícil") // ← Dificultad Difícil
            startActivity(intent)
        }
    }


    override fun onPause() {
        super.onPause()
        if (isJuegoActivo && !isPausado) {
            pausarLogicaJuego()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isJuegoActivo && isPausado) {
            mostrarDialogoPausa()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        animadoresActivos.clear()
    }

    // --- 3. Game Loop ---

    // --- NUEVO: Definimos el Runnable fuera
    private fun definirGameLoop() {
        gameLoop = object : Runnable {
            override fun run() {
                if (!isJuegoActivo || isPausado) return

                val numeroDeObjetos = random.nextInt(2) + 1

                for (i in 1..numeroDeObjetos) {
                    val retrasoSpawn = (i - 1) * 200L
                    gameHandler.postDelayed({
                                                if (isJuegoActivo && !isPausado) {
                                                    crearObjetoQueCae()
                                                }
                                            }, retrasoSpawn)
                }

                // VELOCIDAD DIFICIL: 1.5 segundos entre oleadas
                val proximoDelay = (1500).toLong()
                gameHandler.postDelayed(this, proximoDelay)
            }
        }
    }

    private fun iniciarJuego() {
        establecerNuevaFormaObjetivo()
        gameHandler.post(gameLoop)
    }

    private fun establecerNuevaFormaObjetivo() {
        val nuevaFormaId = imagenesJuego[random.nextInt(imagenesJuego.size)]
        ivTargetShape.setImageResource(nuevaFormaId)
        currentTargetDrawableId = nuevaFormaId
    }

    // --- 4. Crear objeto que cae ---
    private fun crearObjetoQueCae() {
        val objeto = ImageView(this)
        val imagenParaCaerId = imagenesJuego[random.nextInt(imagenesJuego.size)]
        objeto.setImageResource(imagenParaCaerId)
        objeto.tag = imagenParaCaerId

        val tamanoEnPx = (100 * resources.displayMetrics.density).toInt()
        objeto.layoutParams = ConstraintLayout.LayoutParams(tamanoEnPx, tamanoEnPx)

        cieloContainer.post {
            if (cieloContainer.width <= 0) return@post

            val maxWidth = (cieloContainer.width - tamanoEnPx).coerceAtLeast(1)
            val startX = random.nextInt(maxWidth)
            objeto.x = startX.toFloat()
            objeto.y = 0f

            objeto.setOnClickListener { view ->
                // --- NUEVO: Bloquear clic si está pausado
                if (!isJuegoActivo || isPausado) return@setOnClickListener

                cieloContainer.removeView(view)

                if (view.tag as Int == currentTargetDrawableId) {
                    sumarPuntos(10)
                } else {
                    restarVida()
                }
            }

            cieloContainer.addView(objeto)
            animarCaida(objeto)
        }
    }

    // --- 5. Animación de caída ---
    private fun animarCaida(objeto: ImageView) {
        val alturaSuelo = cieloContainer.height.toFloat()
        val animator = ObjectAnimator.ofFloat(objeto, "translationY", 0f, alturaSuelo)

        // VELOCIDAD DIFICIL: 2.5 segundos en caer
        animator.duration = 2500

        // --- NUEVO: Añadir a lista
        animadoresActivos.add(animator)

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) { // Corrección del tipo Animator
                // --- NUEVO: Remover de lista
                animadoresActivos.remove(animation)

                if (cieloContainer.indexOfChild(objeto) != -1) {
                    cieloContainer.removeView(objeto)
                    if (objeto.tag as Int == currentTargetDrawableId) {
                        if (isJuegoActivo) restarVida()
                    }
                }
            }
        })

        animator.start()
    }

    // --- 6. Lógica de puntos y vidas ---
    private fun sumarPuntos(cantidad: Int) {
        puntos += cantidad
        tvPuntos.text = "PUNTOS: $puntos"
        if (puntos > record) {
            record = puntos
        }
    }

    private fun restarVida() {
        vidas--
        tvVidas.text = "VIDAS: $vidas"
        if (vidas <= 0) {
            tvVidas.text = "¡FIN!"
            terminarJuego()
        }
    }

    private fun terminarJuego() {
        isJuegoActivo = false

        // Limpiar todo
        gameHandler.removeCallbacksAndMessages(null)
        for(anim in animadoresActivos) {
            anim.cancel()
        }
        animadoresActivos.clear()

        Handler(Looper.getMainLooper()).postDelayed({
                                                        val intent = Intent(this, FinReinicioActivity::class.java)
                                                        intent.putExtra("PUNTUACION_FINAL", puntos)
                                                        intent.putExtra("RECORD_ACTUAL", record)
                                                        startActivity(intent)
                                                        finish()
                                                    }, 0)
    }
}