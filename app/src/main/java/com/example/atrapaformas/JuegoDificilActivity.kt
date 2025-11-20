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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.Random

class JuegoDificilActivity : AppCompatActivity() {

    // --- 1. Variables del Juego ---
    private var vidas = 3
    private var puntos = 0
    private var record = 0
    private var isJuegoActivo = true
    private var isPausado = false

    private lateinit var tvVidas: TextView
    private lateinit var tvPuntos: TextView
    private lateinit var ivTargetShape: ImageView
    private lateinit var cieloContainer: ConstraintLayout
    private lateinit var btnPause: View

    private var currentTargetDrawableId: Int = 0

    private val gameHandler = Handler(Looper.getMainLooper())
    private val random = Random()
    private var mediaPlayer: MediaPlayer? = null

    // Lista para controlar animaciones activas
    private val animadoresActivos = mutableListOf<ObjectAnimator>()
    private lateinit var gameLoop: Runnable

    // --- VARIABLES DE GESTIÓN DE PARTIDA (Fusionadas) ---
    private lateinit var gestorPartidas: GestorPartidas
    private var idPartida: String = ""
    private var tiempoInicioPartida: Long = 0

    private val imagenesJuego = listOf(
        R.drawable.cuadrado_formas,
        R.drawable.triangulos_formas,
        R.drawable.rombos_formas,
        R.drawable.circulos_formas
                                      )

    // --- 2. Método Principal ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Se usa el layout de juego fácil (común)
        setContentView(R.layout.activity_juego_facil)

        // 1. Inicializar Gestor
        gestorPartidas = GestorPartidas("${filesDir.path}/partidas")
        idPartida = intent.getStringExtra("ID_PARTIDA") ?: ""
        tiempoInicioPartida = System.currentTimeMillis()

        // 2. Música
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
        btnPause = findViewById(R.id.button_pause)

        // Configurar botón de pausa
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

    // --- Lógica del Menú de Pausa ---
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
        gameHandler.removeCallbacks(gameLoop)
        gameHandler.removeCallbacksAndMessages(null)

        for (anim in animadoresActivos) {
            if (anim.isRunning) {
                anim.pause()
            }
        }
    }

    private fun reanudarLogicaJuego() {
        isPausado = false
        mediaPlayer?.start()

        for (anim in animadoresActivos) {
            if (anim.isPaused) {
                anim.resume()
            }
        }
        gameHandler.post(gameLoop)
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
        gameHandler.removeCallbacksAndMessages(null)
    }

    // --- 3. Game Loop ---

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

        animadoresActivos.add(animator)

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Remover de lista
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

    // --- 7. FIN DEL JUEGO (CORREGIDO) ---
    private fun terminarJuego() {
        isJuegoActivo = false

        // Limpiar todo
        gameHandler.removeCallbacksAndMessages(null)

        // --- CORRECCIÓN CRASH: Usar copia de la lista ---
        val copiaAnimadores = animadoresActivos.toList()
        for(anim in copiaAnimadores) {
            anim.cancel()
        }
        animadoresActivos.clear()

        // --- LÓGICA FUSIONADA DEL GESTOR DE PARTIDAS ---
        val tiempoJugadoSegundos = ((System.currentTimeMillis() - tiempoInicioPartida) / 1000).toInt()
        gestorPartidas.finalizarPartida(idPartida, puntos, tiempoJugadoSegundos)

        Handler(Looper.getMainLooper()).postDelayed({
                                                        val intent = Intent(this, FinReinicioActivity::class.java)
                                                        intent.putExtra("PUNTUACION_FINAL", puntos)
                                                        intent.putExtra("ID_PARTIDA", idPartida)
                                                        intent.putExtra("DIFICULTAD", "Difícil")
                                                        startActivity(intent)
                                                        finish()
                                                    }, 500)
    }
}