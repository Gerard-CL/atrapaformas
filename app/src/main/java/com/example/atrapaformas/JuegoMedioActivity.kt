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

class JuegoMedioActivity : AppCompatActivity() {

    // --- Variables del Juego ---
    private var vidas = 3
    private var puntos = 0
    private var record = 0
    private var isJuegoActivo = true
    private var isPausado = false

    // Variable para guardar el nombre y usarlo al final
    private var nombreJugador: String? = null

    private lateinit var hearts: List<ImageView>

    private lateinit var tvPuntos: TextView
    private lateinit var ivTargetShape: ImageView
    private lateinit var cieloContainer: ConstraintLayout
    private lateinit var btnPause: View

    private var currentTargetDrawableId: Int = 0

    private val gameHandler = Handler(Looper.getMainLooper())
    private val random = Random()
    private var mediaPlayer: MediaPlayer? = null

    private val animadoresActivos = mutableListOf<ObjectAnimator>()
    private lateinit var gameLoop: Runnable

    private lateinit var gestorPartidas: GestorPartidas
    private var idPartida: String = ""
    private var tiempoInicioPartida: Long = 0

    private val imagenesJuego = listOf(
        R.drawable.cuadrado_formas,
        R.drawable.triangulos_formas,
        R.drawable.rombos_formas,
        R.drawable.circulos_formas
                                      )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juego_medio)

        gestorPartidas = GestorPartidas("${filesDir.path}/partidas")
        idPartida = intent.getStringExtra("ID_PARTIDA") ?: ""

        // Guardamos el nombre en la variable de clase
        nombreJugador = intent.getStringExtra("NOMBRE_JUGADOR")

        tiempoInicioPartida = System.currentTimeMillis()

        val textViewNombre = findViewById<TextView>(R.id.tv_usuario)
        textViewNombre.text = nombreJugador ?: "Jugador"

        hearts = listOf(
            findViewById(R.id.heart1),
            findViewById(R.id.heart2),
            findViewById(R.id.heart3)
                       )

        tvPuntos = findViewById(R.id.tv_puntos)
        ivTargetShape = findViewById(R.id.iv_target_shape)
        cieloContainer = findViewById(R.id.cielo_container)
        btnPause = findViewById(R.id.button_pause)

        mediaPlayer = MediaPlayer.create(this, R.raw.fondomusica1)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        btnPause.setOnClickListener {
            mostrarDialogoPausa()
        }

        tvPuntos.text = "PUNTOS: $puntos"
        record = 112

        definirGameLoop()
        iniciarJuego()
    }

    // ... Métodos de Pausa ...
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
        if (mediaPlayer?.isPlaying == true) mediaPlayer?.pause()
        gameHandler.removeCallbacks(gameLoop)
        gameHandler.removeCallbacksAndMessages(null)
        for (anim in animadoresActivos) { if (anim.isRunning) anim.pause() }
    }

    private fun reanudarLogicaJuego() {
        isPausado = false
        mediaPlayer?.start()
        for (anim in animadoresActivos) { if (anim.isPaused) anim.resume() }
        gameHandler.post(gameLoop)
    }

    override fun onPause() {
        super.onPause()
        if (isJuegoActivo && !isPausado) pausarLogicaJuego()
    }

    override fun onResume() {
        super.onResume()
        if (isJuegoActivo && isPausado) mostrarDialogoPausa()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        gameHandler.removeCallbacksAndMessages(null)
    }

    // ... GameLoop y Lógica ...

    private fun definirGameLoop() {
        gameLoop = object : Runnable {
            override fun run() {
                if (!isJuegoActivo || isPausado) return
                val numeroDeObjetos = random.nextInt(2) + 1
                for (i in 1..numeroDeObjetos) {
                    val retrasoSpawn = (i - 1) * 200L
                    gameHandler.postDelayed({
                                                if (isJuegoActivo && !isPausado) crearObjetoQueCae()
                                            }, retrasoSpawn)
                }
                val proximoDelay = 2500L
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

    private fun crearObjetoQueCae() {
        val objeto = ImageView(this)
        val probabilidadDeTarget = 40
        val dado = random.nextInt(100)
        val imagenParaCaerId = if (dado < probabilidadDeTarget) {
            currentTargetDrawableId
        } else {
            imagenesJuego[random.nextInt(imagenesJuego.size)]
        }

        objeto.setImageResource(imagenParaCaerId)
        objeto.tag = imagenParaCaerId
        val tamanoEnPx = (150 * resources.displayMetrics.density).toInt()
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

    private fun animarCaida(objeto: ImageView) {
        val alturaSuelo = cieloContainer.height.toFloat()
        val animator = ObjectAnimator.ofFloat(objeto, "translationY", 0f, alturaSuelo)

        val duracionCaida: Long = when (puntos) {
            in 0..50 -> 4000L
            in 51..100 -> 3500L
            in 101..150 -> 3000L
            else -> 2500L
        }

        animator.duration = duracionCaida

        animadoresActivos.add(animator)

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
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

    private fun sumarPuntos(cantidad: Int) {
        puntos += cantidad
        tvPuntos.text = "PUNTOS: $puntos"
        if (puntos > record) record = puntos
    }

    private fun restarVida() {
        vidas--
        if (vidas >= 0 && vidas < hearts.size) {
            hearts[vidas].visibility = View.INVISIBLE
        }
        if (vidas <= 0) {
            terminarJuego()
        }
    }

    // --- FIN DEL JUEGO ---
    private fun terminarJuego() {
        isJuegoActivo = false
        gameHandler.removeCallbacksAndMessages(null)
        val copiaAnimadores = animadoresActivos.toList()
        for (anim in copiaAnimadores) {
            anim.cancel()
        }
        animadoresActivos.clear()

        val tiempoJugadoSegundos = ((System.currentTimeMillis() - tiempoInicioPartida) / 1000).toInt()
        gestorPartidas.finalizarPartida(idPartida, puntos, tiempoJugadoSegundos)

        Handler(Looper.getMainLooper()).postDelayed({
                                                        val intent = Intent(this, FinReinicioActivity::class.java)
                                                        intent.putExtra("PUNTUACION_FINAL", puntos)
                                                        intent.putExtra("ID_PARTIDA", idPartida)
                                                        intent.putExtra("DIFICULTAD", "Medio")
                                                        intent.putExtra("NOMBRE_JUGADOR", nombreJugador)

                                                        startActivity(intent)
                                                        finish()
                                                    }, 0)
    }
}